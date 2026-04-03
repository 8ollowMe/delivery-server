package com.followMe.delivery_server.delivery.infra.exception;

import com.followMe.common.exception.BusinessException;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryErrorCode;

public class OrderClientUnavailableException extends BusinessException {
  public OrderClientUnavailableException() {
    super(DeliveryErrorCode.ORDER_CLIENT_UNAVAILABLE);
  }
}
