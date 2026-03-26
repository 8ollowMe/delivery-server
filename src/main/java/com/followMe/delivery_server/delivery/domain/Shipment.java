package com.followMe.delivery_server.delivery.domain;

import com.followMe.common.entity.BaseAudit;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import com.followMe.delivery_server.delivery.exception.DeliveryErrorCode;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "p_shipment",
    uniqueConstraints = @UniqueConstraint(columnNames = {"delivery_id", "sequence"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment extends BaseAudit {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "delivery_id", nullable = false, updatable = false)
  private Delivery delivery;

  @Column(nullable = false, updatable = false)
  private int sequence;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private ShipmentStatus status = ShipmentStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30, updatable = false)
  private ShipmentType type;

  @Embedded
  @Column(nullable = false, updatable = false)
  private Node fromNode;

  @Embedded
  @Column(nullable = false, updatable = false)
  private Node toNode;

  @Embedded private DeliveryManager deliveryManager;

  private Instant shippedAt;
  private Instant arrivedAt;
  private Instant completedAt;

  private Shipment(Delivery delivery, int sequence, ShipmentType type, Node fromNode, Node toNode) {
    this.delivery = delivery;
    this.sequence = sequence;
    checkTypeAndNodes(type, fromNode, toNode);
    this.type = type;
    this.fromNode = fromNode;
    this.toNode = toNode;
  }

  private void checkTypeAndNodes(ShipmentType type, Node fromNode, Node toNode) {
    switch (type) {
      case HUB_TO_HUB -> {
        if (fromNode.getNodeType() != NodeType.HUB || toNode.getNodeType() != NodeType.HUB) {
          throw DeliveryErrorCode.INVALID_SHIPMENT_NODES.toException();
        }
      }
      case HUB_TO_VENDOR -> {
        if (fromNode.getNodeType() != NodeType.HUB || toNode.getNodeType() != NodeType.VENDOR) {
          throw DeliveryErrorCode.INVALID_SHIPMENT_NODES.toException();
        }
      }
      default -> throw DeliveryErrorCode.INVALID_SHIPMENT_TYPE.toException();
    }
  }

  public static Shipment create(
      Delivery delivery, int sequence, ShipmentType type, Node fromNode, Node toNode) {
    return new Shipment(delivery, sequence, type, fromNode, toNode);
  }

  private void transitionTo(ShipmentStatus next) {
    if (!this.status.canTransitionTo(next)) {
      throw DeliveryErrorCode.INVALID_SHIPMENT_STATUS.toException();
    }
    this.status = next;
  }

  public void assignDeliveryManager(DeliveryManager deliveryManager) {
    this.deliveryManager = deliveryManager;
  }

  public void ship() {
    transitionTo(ShipmentStatus.SHIPPED);
    this.shippedAt = Instant.now();
  }

  public void transit() {
    transitionTo(ShipmentStatus.IN_TRANSIT);
  }

  public void arrive() {
    transitionTo(ShipmentStatus.ARRIVED);
    this.arrivedAt = Instant.now();
  }

  public void complete() {
    transitionTo(ShipmentStatus.COMPLETED);
    this.completedAt = Instant.now();
  }

  public void fail() {
    transitionTo(ShipmentStatus.FAILED);
  }

  public void cancel() {
    transitionTo(ShipmentStatus.CANCELLED);
  }
}
