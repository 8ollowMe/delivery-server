package com.followMe.delivery_server.delivery.domain.event;

import com.followMe.common.event.BaseEvent;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.enums.DomainTypes;
import java.util.UUID;
import lombok.Getter;

@Getter
public class DeliveryAssigned extends BaseEvent {

  private static final String domain = DomainTypes.DELIVERY.getType();

  public record Payload(UUID shipmentId, UUID deliveryManagerId) {}

  private DeliveryAssigned(UUID deliveryId, Payload payload) {
    super(domain, deliveryId, payload);
  }

  public static DeliveryAssigned of(Shipment shipment) {
    return new DeliveryAssigned(
        shipment.getDelivery().getId(),
        new Payload(shipment.getId(), shipment.getDeliveryManager().getId()));
  }
}
