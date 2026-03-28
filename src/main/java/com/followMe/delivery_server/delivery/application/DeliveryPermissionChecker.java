package com.followMe.delivery_server.delivery.application;

import static com.followMe.delivery_server.delivery.application.UserRole.*;

import com.followMe.common.exception.BusinessException;
import com.followMe.common.exception.CommonErrorCode;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import java.util.UUID;

public class DeliveryPermissionChecker {

  public static void checkAccess(UserContext user, DeliveryResponseDto delivery) {
    if (user.role() == MASTER || user.role() == VENDOR) return;
    if (user.role() == HUB_MANAGER) {
      checkIfDeliveryRelatedToUserHub(user, delivery);
      return;
    }
    if (user.role() == DELIVERY_MANAGER) {
      checkIfDeliveryAssignedToUser(user, delivery);
    }
  }

  public static void checkListAccess(UserContext user, DeliverySearchCondition condition) {
    switch (user.role()) {
      case HUB_MANAGER -> condition.setHubId(user.hubId());
      case DELIVERY_MANAGER -> condition.setDeliveryManagerId(user.userId());
      case MASTER, VENDOR -> {}
      default -> throw new BusinessException(CommonErrorCode.FORBIDDEN);
    }
  }

  private static void checkIfDeliveryAssignedToUser(
      UserContext user, DeliveryResponseDto delivery) {
    boolean assigned = true;
    for (var shipment : delivery.shipments()) {
      var deliveryManager = shipment.deliveryManager();
      if (deliveryManager != null && !user.userId().equals(deliveryManager.id())) {
        assigned = false;
        break;
      }
    }
    if (!assigned) throw new BusinessException(CommonErrorCode.FORBIDDEN);
  }

  private static void checkIfDeliveryRelatedToUserHub(
      UserContext user, DeliveryResponseDto delivery) {
    boolean related = true;
    for (var shipment : delivery.shipments()) {
      UUID toHubId = shipment.to() != null ? shipment.to().id() : null;
      UUID fromHubId = shipment.from() != null ? shipment.from().id() : null;
      if (!user.hubId().equals(toHubId) && !user.hubId().equals(fromHubId)) {
        related = false;
        break;
      }
    }
    if (!related) throw new BusinessException(CommonErrorCode.FORBIDDEN);
  }
}
