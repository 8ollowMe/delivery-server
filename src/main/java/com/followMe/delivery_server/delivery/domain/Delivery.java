package com.followMe.delivery_server.delivery.domain;

import com.followMe.common.entity.BaseAudit;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import com.followMe.delivery_server.delivery.domain.exception.ForbiddenException;
import com.followMe.delivery_server.delivery.domain.exception.InvalidDeliveryStatusException;
import com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker;
import com.followMe.delivery_server.delivery.infra.client.dto.HubNodeInfo;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "p_delivery")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Embedded
  @AttributeOverrides({@AttributeOverride(name = "value", column = @Column(name = "order_id"))})
  private OrderId orderId;

  @Column(columnDefinition = "uuid")
  private UUID sourceHubId;

  @Column(columnDefinition = "uuid")
  private UUID destinationHubId;

  private String deliveryAddress;
  private String recipient;
  private String recipientSlackId;

  @OrderBy("sequence ASC")
  @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Shipment> shipments = new ArrayList<>();

  private Delivery(
      UUID orderId,
      UUID sourceHubId,
      String deliveryAddress,
      String recipient,
      String recipientSlackId,
      List<HubNodeInfo> nodes) {
    this.orderId = OrderId.of(orderId);
    this.sourceHubId = sourceHubId;
    this.destinationHubId =
        nodes.stream()
            .filter(n -> n.type() == NodeType.HUB)
            .reduce((a, b) -> b)
            .map(HubNodeInfo::id)
            .orElse(null);
    this.deliveryAddress = deliveryAddress;
    this.recipient = recipient;
    this.recipientSlackId = recipientSlackId;
    this.shipments = Shipment.createList(this, nodes);
  }

  public static Delivery create(
      UUID orderId,
      UUID sourceHubId,
      String deliveryAddress,
      String recipient,
      String recipientSlackId,
      List<HubNodeInfo> nodes) {
    return new Delivery(orderId, sourceHubId, deliveryAddress, recipient, recipientSlackId, nodes);
  }

  public DeliveryStatus getDeliveryStatus() {
    if (this.shipments.stream().anyMatch(s -> s.getStatus() == ShipmentStatus.FAILED)) {
      return DeliveryStatus.FAILED;
    }
    if (this.shipments.stream().allMatch(s -> s.getStatus() == ShipmentStatus.CANCELLED)) {
      return DeliveryStatus.CANCELLED;
    }
    if (this.shipments.stream().allMatch(s -> s.getStatus() == ShipmentStatus.COMPLETED)) {
      return DeliveryStatus.COMPLETED;
    }

    Shipment active =
        this.shipments.stream()
            .filter(
                s ->
                    s.getStatus() != ShipmentStatus.COMPLETED
                        && s.getStatus() != ShipmentStatus.CANCELLED)
            .min(Comparator.comparingInt(Shipment::getSequence))
            .orElse(null);

    if (active == null) return DeliveryStatus.COMPLETED;

    if (active.getType() == ShipmentType.HUB_TO_HUB) {
      return switch (active.getStatus()) {
        case SHIPPED, IN_TRANSIT -> DeliveryStatus.HUB_MOVING;
        case ARRIVED -> DeliveryStatus.DESTINATION_HUB_ARRIVED;
        default -> DeliveryStatus.HUB_WAITING;
      };
    } else {
      return switch (active.getStatus()) {
        case SHIPPED, IN_TRANSIT -> DeliveryStatus.DELIVERING;
        case ARRIVED -> DeliveryStatus.VENDOR_MOVING;
        default -> DeliveryStatus.HUB_WAITING;
      };
    }
  }

  public void checkReadAccess(UserContext user) {
    switch (user.role()) {
      case MASTER, VENDOR -> {}
      case DELIVERY -> {
        boolean isManager =
            shipments.stream()
                .anyMatch(
                    s ->
                        s.getDeliveryManager() != null
                            && s.getDeliveryManager().getId().equals(user.userId()));
        if (!isManager) throw new ForbiddenException();
      }
      case HUB -> {
        boolean hasHub =
            shipments.stream()
                .anyMatch(
                    s ->
                        (s.getFrom() != null && s.getFrom().getId().equals(user.hubId()))
                            || (s.getTo() != null && s.getTo().getId().equals(user.hubId())));
        if (!hasHub) throw new ForbiddenException();
      }
      default -> throw new ForbiddenException();
    }
  }

  public void cancel(UserContext user, DeliveryPermissionChecker permissionChecker) {
    permissionChecker.checkCancelAccess(user, this);
    DeliveryStatus currentStatus = this.getDeliveryStatus();
    if (currentStatus != DeliveryStatus.HUB_WAITING && currentStatus != DeliveryStatus.FAILED) {
      throw new InvalidDeliveryStatusException();
    }
    this.shipments.forEach(Shipment::cancel);
  }

  public void softDelete(UserContext user, DeliveryPermissionChecker permissionChecker) {
    permissionChecker.checkDeleteAccess(user, this);
    super.softDelete(user.userId());
    this.shipments.forEach(shipment -> shipment.softDelete(user.userId()));
  }

  public void cancelBySystem() {
    DeliveryStatus currentStatus = this.getDeliveryStatus();
    if (currentStatus != DeliveryStatus.HUB_WAITING && currentStatus != DeliveryStatus.FAILED) {
      throw new InvalidDeliveryStatusException();
    }
    this.shipments.forEach(Shipment::cancel);
  }
}
