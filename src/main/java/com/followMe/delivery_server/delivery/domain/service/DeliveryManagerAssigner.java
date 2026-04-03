package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.delivery_server.delivery.application.dto.ShipmentRequest;
import com.followMe.delivery_server.delivery.domain.DeliveryManager;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import java.util.UUID;

public interface DeliveryManagerAssigner {
  DeliveryManager assignDeliveryManager(UUID hubId, NodeType type);

  DeliveryManager getDeliveryManager(UUID userId, UUID hubId);
}
