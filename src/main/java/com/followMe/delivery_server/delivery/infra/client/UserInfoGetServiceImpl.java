package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.delivery_server.delivery.domain.UserInfo;
import com.followMe.delivery_server.delivery.domain.service.UserInfoGetService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInfoGetServiceImpl implements UserInfoGetService {
  private final UserClient userClient;

  @Override
  public UserInfo getUserInfo(UUID userId) {
    return userClient.getUserInfo(userId);
  }
}
