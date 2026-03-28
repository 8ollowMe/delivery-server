package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import java.util.UUID;

public record DeliveryListItemDto(
    UUID id,
    UUID orderId,
    DeliveryStatus status,
    DeliveryResponse.NodeResponse fromHub,
    DeliveryResponse.NodeResponse toVendor,
    int totalShipments,
    int completedShipments,
    DeliveryResponse.NodeResponse lastestProgressedNode,
    ShipmentStatus currentProgressStatus)
    implements DeliveryResponse {}
