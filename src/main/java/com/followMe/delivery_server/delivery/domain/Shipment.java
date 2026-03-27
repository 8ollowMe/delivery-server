package com.followMe.delivery_server.delivery.domain;

import com.followMe.common.entity.BaseAudit;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import com.followMe.delivery_server.delivery.exception.DeliveryException.*;
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

}
