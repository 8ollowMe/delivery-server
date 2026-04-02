package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public class ShipmentRequest {
  public record UpdateStatus(@NotNull ShipmentStatus status) {}
}
