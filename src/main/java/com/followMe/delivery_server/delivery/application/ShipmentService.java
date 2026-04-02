package com.followMe.delivery_server.delivery.application;

import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import java.util.List;
import java.util.UUID;

public interface ShipmentService {
  List<DeliveryResponseDto.ShipmentResponse> getShipmentsByDeliveryId(
      UserContext user, UUID deliveryId);

  DeliveryResponseDto.ShipmentResponse getShipment(UserContext user, UUID shipmentId);

  void updateStatus(UserContext user, UUID shipmentId, ShipmentStatus status);
}
