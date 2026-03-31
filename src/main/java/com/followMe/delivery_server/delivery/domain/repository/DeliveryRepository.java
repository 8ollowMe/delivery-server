package com.followMe.delivery_server.delivery.domain.repository;

import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;

public interface DeliveryRepository {
  Delivery save(Delivery delivery);

  Optional<Delivery> findById(UUID id);

  Optional<Delivery> findByOrderId(UUID orderId);

  Optional<Delivery> findByIdFetchShipments(UUID id);

  Optional<Shipment> findShipmentById(UUID shipmentId);

  Map<UUID, Integer> countByDeliveryManagerIds(
      List<UUID> managerIds, List<ShipmentStatus> excludedStatus);
}
