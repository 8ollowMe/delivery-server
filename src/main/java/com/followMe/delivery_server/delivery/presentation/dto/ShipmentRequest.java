package com.followMe.delivery_server.delivery.presentation.dto;

import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;

public class ShipmentRequest {
  public record UpdateStatus(ShipmentStatus status) {}
}
