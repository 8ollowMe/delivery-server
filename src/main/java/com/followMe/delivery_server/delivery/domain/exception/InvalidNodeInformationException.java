package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class InvalidNodeInformationException extends BusinessException {
  public InvalidNodeInformationException() {
    super(DeliveryErrorCode.INVALID_NODE_INFORMATION);
  }
}
