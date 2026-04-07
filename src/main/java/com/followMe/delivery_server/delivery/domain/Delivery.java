package com.followMe.delivery_server.delivery.domain;

import com.followMe.common.entity.BaseAudit;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.exception.ForbiddenException;
import com.followMe.delivery_server.delivery.domain.exception.InvalidDeliveryStatusException;
import com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker;
import jakarta.persistence.*;
import java.util.ArrayList;
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

  @OrderBy("sequence ASC")
  @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Shipment> shipments = new ArrayList<>();

  private Delivery(OrderId orderId, List<Node> nodes) {
    this.orderId = orderId;
    this.shipments = Shipment.createList(this, nodes);
  }

  public static Delivery create(UUID orderId, List<Node> nodes) {
    return new Delivery(OrderId.of(orderId), nodes);
  }

  public DeliveryStatus getDeliveryStatus() {
    if (this.shipments.stream()
        .anyMatch(shipment -> shipment.getStatus() == ShipmentStatus.FAILED)) {
      return DeliveryStatus.FAILED;
    }
    if (this.shipments.stream()
        .allMatch(shipment -> shipment.getStatus() == ShipmentStatus.COMPLETED)) {
      return DeliveryStatus.COMPLETED;
    }
    if (this.shipments.stream()
        .anyMatch(shipment -> ShipmentStatus.isInProgress(shipment.getStatus()))) {
      return DeliveryStatus.IN_PROGRESS;
    }
    if (this.shipments.stream()
        .allMatch(shipment -> shipment.getStatus() == ShipmentStatus.CANCELLED)) {
      return DeliveryStatus.CANCELLED;
    }
    return DeliveryStatus.READY;
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
    if (currentStatus != DeliveryStatus.READY && currentStatus != DeliveryStatus.FAILED) {
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
    if (currentStatus != DeliveryStatus.READY && currentStatus != DeliveryStatus.FAILED) {
      throw new InvalidDeliveryStatusException();
    }
    this.shipments.forEach(Shipment::cancel);
  }
}
