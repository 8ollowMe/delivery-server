package com.followMe.delivery_server.delivery.infra.hub;

import com.followMe.delivery_server.delivery.infra.hub.dto.HubRouteResponse;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryException.HubClientUnavailableException;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryException.HubRouteNotFoundException;
import feign.FeignException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HubClientFallbackFactory implements FallbackFactory<HubClient> {

  @Override
  public HubClient create(Throwable cause) {
    return new HubClient() {
      @Override
      public HubRouteResponse getNodes(UUID sourceHubId, UUID vendorId) {
        log.error(
            "Failed to get hub route for sourceHubId: {}, vendorId: {}. Cause: {}",
            sourceHubId,
            vendorId,
            cause.getMessage());

        if (cause instanceof FeignException.NotFound) {
          throw new HubRouteNotFoundException();
        }
        throw new HubClientUnavailableException();
      }
    };
  }
}
