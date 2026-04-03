package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class DeliveryManagerNotFoundException extends BusinessException {
  public DeliveryManagerNotFoundException() {
    super(DeliveryErrorCode.DELIVERY_MANAGER_NOT_FOUND);
  }
}
