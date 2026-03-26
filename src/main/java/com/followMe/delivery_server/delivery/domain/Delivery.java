package com.followMe.delivery_server.delivery.domain;

import com.followMe.common.entity.BaseAudit;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import com.followMe.delivery_server.delivery.exception.DeliveryErrorCode;
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

  @Column(nullable = false, columnDefinition = "uuid")
  private UUID orderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private DeliveryStatus status;

  @OneToMany(mappedBy = "delivery", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Shipment> shipments = new ArrayList<>();

  private Delivery(UUID orderId, List<ShipmentInit> shipmentRequests) {
    this.orderId = orderId;
    this.status = DeliveryStatus.READY;
    for (ShipmentInit shipmentRequest : shipmentRequests) {
      ShipmentType type = shipmentRequest.shipmentType();
      Node fromNode = shipmentRequest.fromNode();
      Node toNode = shipmentRequest.toNode();
      int sequence = shipmentRequest.sequence();
      Shipment shipment = Shipment.create(this, sequence, type, fromNode, toNode);
      this.shipments.add(shipment);
    }
  }

  public static Delivery create(UUID orderId, List<ShipmentInit> shipmentRequests) {
    return new Delivery(orderId, shipmentRequests);
  }

  public void start() {
    if (!this.status.canTransitionTo(DeliveryStatus.IN_PROGRESS)) {
      throw DeliveryErrorCode.INVALID_DELIVERY_STATUS.toException();
    }
    this.status = DeliveryStatus.IN_PROGRESS;
  }

  public void complete() {
    if (!this.status.canTransitionTo(DeliveryStatus.COMPLETED)) {
      throw DeliveryErrorCode.INVALID_DELIVERY_STATUS.toException();
    }
    this.status = DeliveryStatus.COMPLETED;
  }

  public void fail() {
    if (!this.status.canTransitionTo(DeliveryStatus.FAILED)) {
      throw DeliveryErrorCode.INVALID_DELIVERY_STATUS.toException();
    }
    this.status = DeliveryStatus.FAILED;
  }

  public void cancel() {
    if (!this.status.canTransitionTo(DeliveryStatus.CANCELLED)) {
      throw DeliveryErrorCode.INVALID_DELIVERY_STATUS.toException();
    }
    this.status = DeliveryStatus.CANCELLED;
  }
}
