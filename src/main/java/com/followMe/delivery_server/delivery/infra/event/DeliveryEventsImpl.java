package com.followMe.delivery_server.delivery.infra.event;

import com.followMe.common.event.Events;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.event.DeliveryAssigned;
import com.followMe.delivery_server.delivery.domain.event.DeliveryCompleted;
import com.followMe.delivery_server.delivery.domain.event.DeliveryEvents;
import com.followMe.delivery_server.delivery.domain.event.ShipmentCompleted;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryEventsImpl implements DeliveryEvents {

  @Override
  public void deliveryAssigned(Shipment shipment) {
    Events.trigger(DeliveryAssigned.of(shipment));
  }

  @Override
  public void shipmentCompleted(Shipment shipment) {
    Events.trigger(ShipmentCompleted.of(shipment));
  }

  @Override
  public void deliveryCompleted(Delivery delivery) {
    Events.trigger(DeliveryCompleted.of(delivery));
  }
}
