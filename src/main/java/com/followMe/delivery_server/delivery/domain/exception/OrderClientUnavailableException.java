package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class OrderClientUnavailableException extends BusinessException {
  public OrderClientUnavailableException() {
    super(DeliveryErrorCode.ORDER_CLIENT_UNAVAILABLE);
  }
}
