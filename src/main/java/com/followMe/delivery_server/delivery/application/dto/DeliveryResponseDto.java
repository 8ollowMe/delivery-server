package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryResponseDto(
    UUID id,
    UUID orderId,
    DeliveryStatus status,
    List<ShipmentResponse> shipments,
    LocalDateTime createdAt)
    implements DeliveryResponse {

  public record ShipmentResponse(
      UUID id,
      int sequence,
      ShipmentStatus status,
      ShipmentType type,
      DeliveryResponse.NodeResponse from,
      DeliveryResponse.NodeResponse to,
      DeliveryManagerResponse deliveryManager,
      LocalDateTime shippedAt,
      LocalDateTime arrivedAt,
      LocalDateTime completedAt) {}

  public record DeliveryManagerResponse(UUID id, String name) {}
}
