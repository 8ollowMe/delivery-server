package com.followMe.delivery_server.delivery.infra.exception;

import com.followMe.common.exception.BusinessException;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryErrorCode;

public class HubClientUnavailableException extends BusinessException {
  public HubClientUnavailableException() {
    super(DeliveryErrorCode.HUB_CLIENT_UNAVAILABLE);
  }
}
