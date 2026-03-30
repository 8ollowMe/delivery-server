package com.followMe.delivery_server.delivery.domain;

import com.followMe.common.entity.BaseAudit;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.exception.InvalidDeliveryStatusException;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "p_delivery")
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

  public void cancel() {
    DeliveryStatus currentStatus = this.getDeliveryStatus();
    if (currentStatus != DeliveryStatus.READY && currentStatus != DeliveryStatus.FAILED) {
      throw new InvalidDeliveryStatusException();
    }
    this.shipments.forEach(Shipment::cancel);
  }

  public void softDelete(UUID deletedBy) {
    super.softDelete(deletedBy);
    this.shipments.forEach(shipment -> shipment.softDelete(deletedBy));
  }
}
