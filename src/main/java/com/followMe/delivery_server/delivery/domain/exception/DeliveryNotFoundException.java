package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class DeliveryNotFoundException extends BusinessException {
  public DeliveryNotFoundException() {
    super(DeliveryErrorCode.DELIVERY_NOT_FOUND);
  }
}
