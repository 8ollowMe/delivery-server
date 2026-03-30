package com.followMe.delivery_server.delivery.application;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.dto.*;
import com.followMe.delivery_server.delivery.domain.UserContext;

import java.util.UUID;

public interface DeliveryService {

  DeliveryCreateResponse createDelivery(OrderCreateCommand command);

  DeliveryResponseDto getDelivery(UserContext user, UUID deliveryId);

  PageResponse<DeliveryListItemDto> getDeliveries(
      UserContext user, PageRequest pageRequest, DeliverySearchCondition condition);

  DeliveryResponseDto getDeliveryByOrderId(UserContext user, UUID orderId);

  void cancelDelivery(UserContext user, UUID deliveryId);

  void deleteDelivery(UserContext user, UUID deliveryId);
}
