package com.followMe.delivery_server.delivery.domain;

import com.followMe.common.entity.BaseAudit;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.exception.DeliveryException.InvalidDeliveryStatusException;
import jakarta.persistence.*;
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
  private OrderId orderId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private DeliveryStatus status;

  private Delivery(OrderId orderId) {
    this.orderId = orderId;
    this.status = DeliveryStatus.READY;
  }

  public static Delivery create(UUID orderId) {
    return new Delivery(OrderId.of(orderId));
  }

  public void start() {
    if (this.status.isTransitionNotAllowed(DeliveryStatus.IN_PROGRESS)) {
      throw new InvalidDeliveryStatusException();
    }
    this.status = DeliveryStatus.IN_PROGRESS;
  }

  public void complete() {
    if (this.status.isTransitionNotAllowed(DeliveryStatus.COMPLETED)) {
      throw new InvalidDeliveryStatusException();
    }
    this.status = DeliveryStatus.COMPLETED;
  }

  public void fail() {
    if (this.status.isTransitionNotAllowed(DeliveryStatus.FAILED)) {
      throw new InvalidDeliveryStatusException();
    }
    this.status = DeliveryStatus.FAILED;
  }

  public void cancel() {
    if (this.status.isTransitionNotAllowed(DeliveryStatus.CANCELLED)) {
      throw new InvalidDeliveryStatusException();
    }
    this.status = DeliveryStatus.CANCELLED;
  }

  public void softDelete(UUID deletedBy) {
    super.softDelete(deletedBy.toString());
  }
}
