package com.followMe.delivery_server.delivery.infra.exception;

import com.followMe.common.exception.BusinessException;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryErrorCode;

public class HubRouteNotFoundException extends BusinessException {
  public HubRouteNotFoundException() {
    super(DeliveryErrorCode.HUB_ROUTE_NOT_FOUND);
  }
}
