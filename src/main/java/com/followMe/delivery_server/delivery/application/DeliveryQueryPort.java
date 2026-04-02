package com.followMe.delivery_server.delivery.application;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponse;
import com.followMe.delivery_server.delivery.application.dto.ShipmentResponse;
import com.followMe.delivery_server.delivery.domain.DeliverySearchCondition;
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
