package com.followMe.delivery_server.delivery.repository;

import com.followMe.delivery_server.delivery.domain.Delivery;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {}
