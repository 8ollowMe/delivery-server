package com.followMe.delivery_server.delivery.domain;

import com.followMe.common.entity.BaseAudit;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import com.followMe.delivery_server.delivery.domain.event.DeliveryEvents;
import com.followMe.delivery_server.delivery.domain.exception.*;
import com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(
    name = "p_shipment",
    uniqueConstraints = @UniqueConstraint(columnNames = {"delivery_id", "sequence"}))
@SQLRestriction("deleted_at IS NULL")
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
  @AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "from_node_id")),
    @AttributeOverride(name = "type", column = @Column(name = "from_node_type")),
    @AttributeOverride(name = "name", column = @Column(name = "from_node_name"))
  })
  private Node from;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "to_node_id")),
    @AttributeOverride(name = "type", column = @Column(name = "to_node_type")),
    @AttributeOverride(name = "name", column = @Column(name = "to_node_name"))
  })
  private Node to;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "id", column = @Column(name = "delivery_manager_id")),
    @AttributeOverride(name = "name", column = @Column(name = "delivery_manager_name"))
  })
  private DeliveryManager deliveryManager;

  @Version private int _version;

  private BigDecimal estimatedDistance;
  private BigDecimal estimatedDuration;
  private BigDecimal actualDistance;
  private BigDecimal actualDuration;

  private Instant shippedAt;
  private Instant arrivedAt;
  private Instant completedAt;

  private Shipment(
      Delivery delivery,
      int sequence,
      Node fromNode,
      Node toNode,
      BigDecimal estimatedDistance,
      BigDecimal estimatedDuration) {
    this.delivery = delivery;
    this.sequence = sequence;
    this.type = resolveType(fromNode, toNode);
    this.from = fromNode;
    this.to = toNode;
    this.estimatedDistance = estimatedDistance;
    this.estimatedDuration = estimatedDuration;
  }

  public static List<Shipment> createList(Delivery delivery, List<RouteNode> nodes) {
    if (nodes == null || nodes.size() < 2) throw new InvalidNodeInformationException();
    if (nodes.getLast().type() != NodeType.VENDOR) throw new InvalidNodeInformationException();
    if (nodes.stream().limit(nodes.size() - 1).anyMatch(n -> n.type() == NodeType.VENDOR))
      throw new InvalidNodeInformationException();

    List<Shipment> shipments = new ArrayList<>();
    for (int i = 0; i < nodes.size() - 1; i++) {
      RouteNode from = nodes.get(i);
      RouteNode to = nodes.get(i + 1);
      shipments.add(
          new Shipment(
              delivery,
              i + 1,
              Node.of(from.type(), from.id(), from.name()),
              Node.of(to.type(), to.id(), to.name()),
              from.distance(),
              from.duration()));
    }
    return shipments;
  }

  private static ShipmentType resolveType(Node from, Node to) {
    if (from.getType() == NodeType.HUB && to.getType() == NodeType.HUB)
      return ShipmentType.HUB_TO_HUB;
    if (from.getType() == NodeType.HUB && to.getType() == NodeType.VENDOR)
      return ShipmentType.HUB_TO_VENDOR;
    throw new NodeTypeMismatchException();
  }

  private void transitionTo(ShipmentStatus next) {
    if (!this.status.canTransitionTo(next)) {
      throw new InvalidShipmentStatusException();
    }
    this.status = next;
  }

  public void checkReadAccess(UserContext user) {
    switch (user.role()) {
      case MASTER, VENDOR -> {}
      case DELIVERY -> {
        if (deliveryManager == null || !deliveryManager.getId().equals(user.userId()))
          throw new ForbiddenException();
      }
      case HUB -> {
        boolean hasHub =
            (from != null && from.getId().equals(user.hubId()))
                || (to != null && to.getId().equals(user.hubId()));
        if (!hasHub) throw new ForbiddenException();
      }
      default -> throw new ForbiddenException();
    }
  }

  public void reassignDeliveryManager(
      UserContext user,
      DeliveryManager deliveryManager,
      DeliveryPermissionChecker permissionChecker) {
    permissionChecker.checkShipmentManagerReassignAccess(user, this);
    if (this.status == ShipmentStatus.PENDING || this.status == ShipmentStatus.FAILED) {
      this.deliveryManager = deliveryManager;
    } else {
      throw new InvalidShipmentStatusException();
    }
  }

  public void assignDeliveryManager(DeliveryManager deliveryManager, DeliveryEvents events) {
    if (this.status == ShipmentStatus.PENDING || this.status == ShipmentStatus.FAILED) {
      setDeliveryManager(deliveryManager, events);
    } else {
      throw new InvalidShipmentStatusException();
    }
  }

  private void setDeliveryManager(DeliveryManager deliveryManager, DeliveryEvents events) {
    this.deliveryManager = deliveryManager;
    events.deliveryAssigned(this);
  }

  public void ship() {
    if (sequence > 1) {
      Shipment prev = getPreviousShipment();
      if (prev.getStatus() != ShipmentStatus.COMPLETED) {
        throw new InvalidShipmentStatusException();
      }
    }
    transitionTo(ShipmentStatus.SHIPPED);
    this.shippedAt = Instant.now();
  }

  private Shipment getPreviousShipment() {
    Shipment prev;
    for (Shipment shipment : delivery.getShipments()) {
      if (shipment.getSequence() == this.sequence - 1) {
        prev = shipment;
        return prev;
      }
    }
    throw new ShipmentNotFoundException();
  }

  public void transit() {
    transitionTo(ShipmentStatus.IN_TRANSIT);
  }

  public void arrive() {
    transitionTo(ShipmentStatus.ARRIVED);
    this.arrivedAt = Instant.now();
  }

  public void complete(DeliveryEvents events) {
    transitionTo(ShipmentStatus.COMPLETED);
    this.completedAt = Instant.now();
    if (this.shippedAt != null) {
      this.actualDuration =
          BigDecimal.valueOf(Duration.between(this.shippedAt, this.completedAt).toMinutes());
    }
    if (type == ShipmentType.HUB_TO_VENDOR) {
      events.deliveryCompleted(delivery);
    } else events.shipmentCompleted(this);
  }

  public void recordActualDistance(BigDecimal distance) {
    this.actualDistance = distance;
  }

  public void fail() {
    transitionTo(ShipmentStatus.FAILED);
  }

  public void cancel() {
    transitionTo(ShipmentStatus.CANCELLED);
  }

  public void updateShipmentStatus(
      ShipmentStatus status,
      UserContext user,
      DeliveryPermissionChecker permissionChecker,
      DeliveryEvents events) {

    permissionChecker.checkShipmentStatusUpdateAccess(user, this);
    switch (status) {
      case SHIPPED -> this.ship();

      case IN_TRANSIT -> this.transit();
      case ARRIVED -> this.arrive();
      case COMPLETED -> this.complete(events);
      case FAILED -> this.fail();
      default -> throw new InvalidShipmentStatusException();
    }
  }
}
