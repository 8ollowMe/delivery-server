package com.followMe.delivery_server.delivery.domain.event;

import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;

public interface DeliveryEvents {
  void deliveryAssigned(Shipment shipment);

  void shipmentCompleted(Shipment shipment);

  void deliveryCompleted(Delivery delivery);
}
