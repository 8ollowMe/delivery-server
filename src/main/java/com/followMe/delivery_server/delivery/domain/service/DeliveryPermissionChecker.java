package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.UserContext;

public interface DeliveryPermissionChecker {

  void checkCancelAccess(UserContext user, Delivery delivery);

  void checkDeleteAccess(UserContext user, Delivery delivery);

  void checkShipmentStatusUpdateAccess(UserContext user, Shipment shipment);

  void checkShipmentManagerReassignAccess(UserContext user, Shipment shipment);
}
