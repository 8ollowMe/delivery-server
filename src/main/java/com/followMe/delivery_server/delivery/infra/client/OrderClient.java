package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.delivery_server.delivery.presentation.dto.OrderRequest;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "order-server",
    fallbackFactory = OrderClientFallbackFactory.class,
    primary = false)
public interface OrderClient {
  @PatchMapping("/internal/v1/orders/{orderId}/delivery")
  void deliveryManagerAssigned(
      @PathVariable UUID orderId, @RequestBody OrderRequest.DeliveryAssigned request);
}
