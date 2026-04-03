package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.delivery_server.delivery.domain.DeliveryManager;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import java.util.UUID;

public interface DeliveryManagerAssigner {
  DeliveryManager assignDeliveryManager(ShipmentType type, UUID hubId);
}
