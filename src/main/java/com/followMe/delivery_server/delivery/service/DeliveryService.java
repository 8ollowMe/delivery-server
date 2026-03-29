package com.followMe.delivery_server.delivery.service;

import com.followMe.delivery_server.delivery.dto.OrderCreateCommand;

public interface DeliveryService {

  void createDelivery(OrderCreateCommand command);
}
