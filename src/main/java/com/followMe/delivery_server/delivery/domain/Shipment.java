package com.followMe.delivery_server.delivery.domain;

import com.followMe.common.entity.BaseAudit;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import com.followMe.delivery_server.delivery.exception.DeliveryException.InvalidShipmentStatusException;
import com.followMe.delivery_server.delivery.exception.DeliveryException.NodeTypeMismatchException;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

  private Instant shippedAt;
  private Instant arrivedAt;
  private Instant completedAt;

  private Shipment(Delivery delivery, int sequence, Node fromNode, Node toNode) {
    this.delivery = delivery;
    this.sequence = sequence;
    this.type = resolveType(fromNode, toNode);
    this.from = fromNode;
    this.to = toNode;
  }

  public static Shipment create(Delivery delivery, int sequence, Node fromNode, Node toNode) {
    return new Shipment(delivery, sequence, fromNode, toNode);
  }

  public static List<Shipment> createList(Delivery delivery, List<Node> nodes) {
    List<Shipment> shipments = new ArrayList<>();

    if (nodes.getLast().getType() != NodeType.VENDOR) throw new NodeTypeMismatchException();
    if (nodes.stream().limit(nodes.size() - 1).anyMatch(n -> n.getType() == NodeType.VENDOR))
      throw new NodeTypeMismatchException();

    for (int i = 0; i < nodes.size() - 1; i++) {
      Node fromNode = nodes.get(i);
      Node toNode = nodes.get(i + 1);
      Shipment shipment = new Shipment(delivery, i + 1, fromNode, toNode);
      shipments.add(shipment);
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
