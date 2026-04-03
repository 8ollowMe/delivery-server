package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class UserClientUnavailableException extends BusinessException {
  public UserClientUnavailableException() {
    super(DeliveryErrorCode.USER_CLIENT_UNAVAILABLE);
  }
}
