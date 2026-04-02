package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.UserContext;
import com.followMe.delivery_server.delivery.presentation.dto.ShipmentResponse;
import java.util.List;

public interface DeliveryPermissionChecker {

  void checkReadAccess(UserContext user, List<ShipmentResponse.Detail> shipments);

  void checkCancelAccess(UserContext user, Delivery delivery);

  void checkDeleteAccess(UserContext user, Delivery delivery);

  void checkListAccess(UserContext user, DeliverySearchCondition condition);

  void checkShipmentStatusUpdateAccess(UserContext user, Shipment shipment);
}
