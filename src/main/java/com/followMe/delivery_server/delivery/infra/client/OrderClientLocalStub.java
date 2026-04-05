package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.delivery_server.delivery.application.dto.OrderRequest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@Profile({"local", "local-intellij"})
public class OrderClientLocalStub implements OrderClient {

  @Override
  public void deliveryManagerAssigned(UUID orderId, OrderRequest.DeliveryAssigned request) {
    log.debug(
        "[LocalStub] OrderClient.deliveryManagerAssigned orderId={} deliveryManagerId={}",
        orderId,
        request.deliveryManagerId());
  }
}