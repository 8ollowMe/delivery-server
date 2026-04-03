package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ShipmentRequest {
  public record UpdateStatus(@NotNull ShipmentStatus status) {}

  public record UpdateDeliveryManager(@NotNull UUID newManagerId) {}
}
