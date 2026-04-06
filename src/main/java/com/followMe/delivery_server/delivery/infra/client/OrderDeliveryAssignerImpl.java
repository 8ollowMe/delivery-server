package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.delivery_server.delivery.application.dto.OrderRequest;
import com.followMe.delivery_server.delivery.domain.service.OrderDeliveryAssigner;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeliveryAssignerImpl implements OrderDeliveryAssigner {

  private final OrderClient client;

  @Override
  public void assignDeliveryToOrder(UUID orderId, UUID deliveryManagerId) {
    client.deliveryManagerAssigned(orderId, new OrderRequest.DeliveryAssigned(deliveryManagerId));
  }
}
