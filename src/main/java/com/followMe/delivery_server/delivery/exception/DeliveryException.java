package com.followMe.delivery_server.delivery.exception;

import com.followMe.common.exception.BusinessException;

public class DeliveryException {

  public static class DeliveryNotFoundException extends BusinessException {
    public DeliveryNotFoundException() {
      super(DeliveryErrorCode.DELIVERY_NOT_FOUND);
    }
  }

  public static class DeliveryAlreadyCompletedException extends BusinessException {
    public DeliveryAlreadyCompletedException() {
      super(DeliveryErrorCode.DELIVERY_ALREADY_COMPLETED);
    }
  }

  public static class InvalidDeliveryStatusException extends BusinessException {
    public InvalidDeliveryStatusException() {
      super(DeliveryErrorCode.INVALID_DELIVERY_STATUS);
    }
  }
}
