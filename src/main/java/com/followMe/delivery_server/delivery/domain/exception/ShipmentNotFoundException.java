package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class ShipmentNotFoundException extends BusinessException {
  public ShipmentNotFoundException() {
    super(DeliveryErrorCode.SHIPMENT_NOT_FOUND);
  }
}
