package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class NodeTypeMismatchException extends BusinessException {
  public NodeTypeMismatchException() {
    super(DeliveryErrorCode.NODE_TYPE_MISMATCH);
  }
}
