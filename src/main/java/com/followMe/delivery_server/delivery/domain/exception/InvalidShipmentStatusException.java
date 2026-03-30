package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class InvalidShipmentStatusException extends BusinessException {
  public InvalidShipmentStatusException() {
    super(DeliveryErrorCode.INVALID_SHIPMENT_STATUS);
  }
}
