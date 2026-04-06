package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.delivery_server.delivery.infra.client.dto.HubRouteResponse;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "hub-server", fallbackFactory = HubClientFallbackFactory.class, primary = false)
public interface HubClient {
  @GetMapping("/internal/hubs/route")
  HubRouteResponse getNodes(@RequestParam UUID sourceHubId, @RequestParam UUID vendorId);
}
