package com.followMe.delivery_server.delivery.application;

import com.followMe.delivery_server.delivery.application.dto.OrderCreateCommand;

public interface DeliveryService {

  void createDelivery(OrderCreateCommand command);

}
