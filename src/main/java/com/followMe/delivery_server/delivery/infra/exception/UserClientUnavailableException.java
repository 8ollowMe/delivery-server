package com.followMe.delivery_server.delivery.infra.exception;

import com.followMe.common.exception.BusinessException;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryErrorCode;

public class UserClientUnavailableException extends BusinessException {
  public UserClientUnavailableException() {
    super(DeliveryErrorCode.USER_CLIENT_UNAVAILABLE);
  }
}
