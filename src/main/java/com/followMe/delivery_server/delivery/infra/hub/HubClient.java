package com.followMe.delivery_server.delivery.infra.hub;

import com.followMe.delivery_server.delivery.infra.hub.dto.HubRouteResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hub-service", fallbackFactory = HubClientFallbackFactory.class)
public interface HubClient {
  @GetMapping("/api/hubs/route")
  HubRouteResponse getNodes(@RequestParam UUID sourceHubId, @RequestParam UUID vendorId);
}
