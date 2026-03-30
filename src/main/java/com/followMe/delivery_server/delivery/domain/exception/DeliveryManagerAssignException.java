package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class DeliveryManagerAssignException extends BusinessException {
  public DeliveryManagerAssignException() {
    super(DeliveryErrorCode.DELIVERY_MANAGER_ASSIGN_FAILED);
  }
}
