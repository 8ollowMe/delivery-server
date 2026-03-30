package com.followMe.delivery_server.delivery.domain.exception;

import com.followMe.common.exception.BusinessException;

public class HubRouteNotFoundException extends BusinessException {
  public HubRouteNotFoundException() {
    super(DeliveryErrorCode.HUB_ROUTE_NOT_FOUND);
  }
}
