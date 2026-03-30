package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class InvalidDeliveryStatusException extends BusinessException {
  public InvalidDeliveryStatusException() {
    super(DeliveryErrorCode.INVALID_DELIVERY_STATUS);
  }
}
