package com.followMe.delivery_server.delivery.domain.event;

import com.followMe.common.event.BaseEvent;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.enums.DomainTypes;
import java.util.UUID;

public class ShipmentCompleted extends BaseEvent {

  private static final String domain = DomainTypes.DELIVERY.getType();

  public record Payload(UUID deliveryId, UUID shipmentId) {}

  private ShipmentCompleted(UUID deliveryId, ShipmentCompleted.Payload payload) {
    super(domain, deliveryId, payload);
  }

  public static ShipmentCompleted of(Shipment shipment) {
    return new ShipmentCompleted(
        shipment.getDelivery().getId(),
        new ShipmentCompleted.Payload(shipment.getDelivery().getId(), shipment.getId()));
  }
}
