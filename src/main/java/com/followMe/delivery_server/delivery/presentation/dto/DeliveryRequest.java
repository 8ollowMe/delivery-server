package com.followMe.delivery_server.delivery.presentation.dto;

import java.util.UUID;

public class DeliveryRequest {
  public record Create(UUID orderId, UUID sourceHubId, UUID vendorId) {}
}
