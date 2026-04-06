package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.delivery_server.delivery.presentation.dto.OrderRequest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {

  @Override
  public OrderClient create(Throwable cause) {
    return new OrderClient() {
      @Override
      public void deliveryManagerAssigned(UUID orderId, OrderRequest.DeliveryAssigned request) {
        log.error(
            "OrderClient.deliveryManagerAssigned fallback triggered for orderId: {}. Cause: {}",
            orderId,
            cause.getMessage());
        // Fallback logic can be implemented here, such as logging or throwing a custom exception
      }
    };
  }
}
