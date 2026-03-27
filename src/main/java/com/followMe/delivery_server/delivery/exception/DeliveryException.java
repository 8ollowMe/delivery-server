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

  public static class ShipmentNotFoundException extends BusinessException {
    public ShipmentNotFoundException() {
      super(DeliveryErrorCode.SHIPMENT_NOT_FOUND);
    }
  }

  public static class ShipmentAlreadyCompletedException extends BusinessException {
    public ShipmentAlreadyCompletedException() {
      super(DeliveryErrorCode.SHIPMENT_ALREADY_COMPLETED);
    }
  }

  public static class InvalidShipmentStatusException extends BusinessException {
    public InvalidShipmentStatusException() {
      super(DeliveryErrorCode.INVALID_SHIPMENT_STATUS);
    }
  }

  public static class NodeTypeMismatchException extends BusinessException {
    public NodeTypeMismatchException() {
      super(DeliveryErrorCode.NODE_TYPE_MISMATCH);
    }
  }

  public static class InvalidShipmentTypeException extends BusinessException {
    public InvalidShipmentTypeException() {
      super(DeliveryErrorCode.INVALID_SHIPMENT_TYPE);
    }
  }
}
