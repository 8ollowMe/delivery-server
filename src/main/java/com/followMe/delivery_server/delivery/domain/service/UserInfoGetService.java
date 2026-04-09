package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.delivery_server.delivery.domain.UserInfo;
import java.util.UUID;

public interface UserInfoGetService {
  UserInfo getUserInfo(UUID userId);
}
