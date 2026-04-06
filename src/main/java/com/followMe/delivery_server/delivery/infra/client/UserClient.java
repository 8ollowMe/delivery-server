package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.domain.DeliveryManagerInfo;
import com.followMe.delivery_server.delivery.domain.UserInfo;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import java.util.List;
import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "user-server",
    fallbackFactory = UserClientFallbackFactory.class,
    primary = false)
public interface UserClient {
  @GetMapping("/internal/v1/users/deliveries")
  List<DeliveryManagerInfo> getDeliveryManagers(
      @RequestParam UUID hubId, @RequestParam NodeType type);

  @GetMapping("/internal/v1/users/{userId}")
  UserInfo getUserInfo(@PathVariable UUID userId);

  @PatchMapping("/internal/v1/users/{userId}/sequence/last")
  ResponseEntity<ApiResponse> updateDeliverySequence(@PathVariable UUID userId);
}
