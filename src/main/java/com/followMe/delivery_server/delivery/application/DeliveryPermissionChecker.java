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
}
