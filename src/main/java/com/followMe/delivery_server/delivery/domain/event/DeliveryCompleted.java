package com.followMe.delivery_server.delivery.domain.event;

import com.followMe.common.event.BaseEvent;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.enums.DomainTypes;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DeliveryCompleted extends BaseEvent {

  private static final String domain = DomainTypes.DELIVERY.getType();

  public record Payload(UUID orderId) {}

  private DeliveryCompleted(UUID deliveryId, Payload payload) {
    super(domain, deliveryId, payload);
  }

  public static DeliveryCompleted of(Delivery delivery) {
    UUID orderId = delivery.getOrderId() != null ? delivery.getOrderId().getValue() : null;
    return new DeliveryCompleted(delivery.getId(), new Payload(orderId));
  }
}
