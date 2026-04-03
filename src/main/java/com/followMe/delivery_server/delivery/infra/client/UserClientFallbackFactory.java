package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.domain.DeliveryManagerInfo;
import com.followMe.delivery_server.delivery.domain.UserInfo;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryManagerAssignException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

  @Override
  public UserClient create(Throwable cause) {
    return new UserClient() {
      @Override
      public List<DeliveryManagerInfo> getDeliveryManagers(UUID hubId, NodeType type) {
        log.error("UserClient.getDeliveryManagers fallback triggered: {}", cause.getMessage());
        throw new DeliveryManagerAssignException();
      }

      @Override
      public UserInfo getUserInfo(UUID userId) {
        log.error("UserClient.getUserInfo fallback triggered: {}", cause.getMessage());
        throw new DeliveryManagerAssignException();
      }

      @Override
      public ResponseEntity<ApiResponse> updateDeliverySequence(UUID userId) {
        log.error("UserClient.updateDeliverySequence fallback triggered: {}", cause.getMessage());
        throw new DeliveryManagerAssignException();
      }
    };
  }
}
