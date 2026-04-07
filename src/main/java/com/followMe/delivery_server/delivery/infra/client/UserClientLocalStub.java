package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.common.response.ApiResponse;
import com.followMe.delivery_server.delivery.domain.DeliveryManagerInfo;
import com.followMe.delivery_server.delivery.domain.UserInfo;
import com.followMe.delivery_server.delivery.domain.UserRole;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.infra.exception.HubClientUnavailableException;
import com.followMe.delivery_server.delivery.infra.exception.UserClientUnavailableException;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@Profile({"local", "local-intellij"})
public class UserClientLocalStub implements UserClient {

  private static final UUID UNKNOWN_USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000099");

  // 로컬 테스트 시드 허브 ID (허브 조건 검증 통과용)
  private static final UUID DEFAULT_HUB_ID =
      UUID.fromString("dddddddd-0000-0000-0000-000000000001");

  @Override
  public List<DeliveryManagerInfo> getDeliveryManagers(UUID hubId, NodeType type) {
    log.debug("[LocalStub] UserClient.getDeliveryManagers hubId={} type={}", hubId, type);
    if (hubId.equals(UNKNOWN_USER_ID)) {
      throw new HubClientUnavailableException();
    }
    return List.of(
        new DeliveryManagerInfo(UUID.randomUUID(), "김배달", 1),
        new DeliveryManagerInfo(UUID.randomUUID(), "이배송", 2),
        new DeliveryManagerInfo(UUID.randomUUID(), "박전달", 3),
        new DeliveryManagerInfo(UUID.randomUUID(), "최운송", 4),
        new DeliveryManagerInfo(UUID.randomUUID(), "정택배", 5),
        new DeliveryManagerInfo(UUID.randomUUID(), "강물류", 6),
        new DeliveryManagerInfo(UUID.randomUUID(), "조배송", 7),
        new DeliveryManagerInfo(UUID.randomUUID(), "윤배달", 8),
        new DeliveryManagerInfo(UUID.randomUUID(), "장전달", 9),
        new DeliveryManagerInfo(UUID.randomUUID(), "임운송", 10));
  }

  @Override
  public UserInfo getUserInfo(UUID userId) {
    log.debug("[LocalStub] UserClient.getUserInfo userId={}", userId);
    if (userId.equals(UNKNOWN_USER_ID)) {
      throw new UserClientUnavailableException();
    }
    return new UserInfo(
        userId, "테스트 사용자", "slackId-00001", DEFAULT_HUB_ID, UUID.randomUUID(), UserRole.DELIVERY);
  }

  @Override
  public ResponseEntity<ApiResponse> updateDeliverySequence(UUID userId) {
    log.debug("[LocalStub] UserClient.updateDeliverySequence userId={}", userId);
    if (userId.equals(UNKNOWN_USER_ID)) {
      throw new UserClientUnavailableException();
    }
    return ResponseEntity.ok(ApiResponse.ok().getBody());
  }
}
