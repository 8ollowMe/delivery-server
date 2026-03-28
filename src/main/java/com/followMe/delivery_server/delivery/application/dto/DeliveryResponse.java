package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import java.util.UUID;

public interface DeliveryResponse {

  UUID id();

  UUID orderId();

  DeliveryStatus status();

  record NodeResponse(UUID id, NodeType type, String name) {}
}
