package com.followMe.delivery_server.delivery.presentation.dto;

import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import java.time.LocalDateTime;
import java.util.UUID;

public class ShipmentResponse {

  public record Detail(
      UUID id,
      int sequence,
      ShipmentStatus status,
      ShipmentType type,
      DeliveryResponse.NodeInfo from,
      DeliveryResponse.NodeInfo to,
      DeliveryResponse.ManagerInfo deliveryManager,
      LocalDateTime shippedAt,
      LocalDateTime arrivedAt,
      LocalDateTime completedAt) {}
}
