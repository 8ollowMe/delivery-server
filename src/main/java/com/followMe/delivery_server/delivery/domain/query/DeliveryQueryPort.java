package com.followMe.delivery_server.delivery.domain.query;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.presentation.dto.DeliveryResponse;
import com.followMe.delivery_server.delivery.presentation.dto.ShipmentResponse;
import java.util.List;
import java.util.UUID;

public interface DeliveryQueryPort {
  DeliveryResponse.Detail findDeliveryById(UUID id);

  PageResponse<DeliveryResponse.ListItem> findDeliveries(
      PageRequest pageRequest, DeliverySearchCondition condition);

  DeliveryResponse.Detail findDeliveryByOrderId(UUID orderId);

  List<ShipmentResponse.Detail> findShipmentsByDeliveryId(UUID deliveryId);

  ShipmentResponse.Detail findShipmentById(UUID shipmentId);
}
