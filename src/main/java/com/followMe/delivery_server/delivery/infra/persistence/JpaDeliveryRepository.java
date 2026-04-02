package com.followMe.delivery_server.delivery.infra.persistence;

import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaDeliveryRepository extends DeliveryRepository, JpaRepository<Delivery, UUID> {

  @Query("SELECT DISTINCT d FROM Delivery d LEFT JOIN FETCH d.shipments WHERE d.id = :id")
  Optional<Delivery> findById(UUID id);

  @Query(
      "SELECT DISTINCT d FROM Delivery d LEFT JOIN FETCH d.shipments WHERE d.orderId.value = :orderId")
  Optional<Delivery> findByOrderId(UUID orderId);

  @Query("SELECT s FROM Shipment s JOIN FETCH s.delivery WHERE s.id = :shipmentId")
  Optional<Shipment> findShipmentById(UUID shipmentId);
}
