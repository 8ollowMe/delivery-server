package com.followMe.delivery_server.delivery.domain.service;

import java.util.UUID;

public interface OrderDeliveryAssigner {
  void assignDeliveryToOrder(UUID orderId, UUID deliveryManagerId);
}
