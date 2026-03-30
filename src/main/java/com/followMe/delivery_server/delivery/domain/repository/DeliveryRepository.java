package com.followMe.delivery_server.delivery.domain.repository;

import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository {
  Delivery save(Delivery delivery);

  Optional<Delivery> findById(UUID id);

  Optional<Shipment> findShipmentById(UUID shipmentId);
}
