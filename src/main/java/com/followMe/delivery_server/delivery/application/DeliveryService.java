package com.followMe.delivery_server.delivery.application;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryCreateResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.application.dto.OrderCreateCommand;
import java.util.UUID;

public interface DeliveryService {

  DeliveryCreateResponse createDelivery(OrderCreateCommand command);

  DeliveryResponseDto getDelivery(UUID deliveryId);

  PageResponse<DeliveryResponseDto> getDeliveries(
      PageRequest pageRequest, DeliverySearchCondition condition);
}
