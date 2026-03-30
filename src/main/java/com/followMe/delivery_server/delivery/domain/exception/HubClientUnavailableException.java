package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class HubClientUnavailableException extends BusinessException {
  public HubClientUnavailableException() {
    super(DeliveryErrorCode.HUB_CLIENT_UNAVAILABLE);
  }
}
