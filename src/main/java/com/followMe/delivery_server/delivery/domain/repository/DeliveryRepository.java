package com.followMe.delivery_server.delivery.domain.repository;

import com.followMe.delivery_server.delivery.domain.Delivery;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {

  @Query("SELECT DISTINCT d FROM Delivery d LEFT JOIN FETCH d.shipments WHERE d.id = :id")
  Optional<Delivery> findById(UUID id);
}
