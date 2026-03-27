package com.followMe.delivery_server.delivery.service;

import com.followMe.delivery_server.delivery.dto.OrderCreateCommand;
import org.springframework.transaction.annotation.Transactional;

public interface DeliveryService {

  void createDelivery(OrderCreateCommand command);
}
