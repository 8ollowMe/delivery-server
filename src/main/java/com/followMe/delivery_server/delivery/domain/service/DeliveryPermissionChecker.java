package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.common.exception.BusinessException;
import com.followMe.common.exception.CommonErrorCode;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.UserContext;
import com.followMe.delivery_server.delivery.presentation.dto.ShipmentResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DeliveryPermissionChecker {

  public static void checkReadAccess(UserContext user, List<ShipmentResponse.Detail> shipments) {
    Set<UUID> nodeIds = new HashSet<>();
    Set<UUID> managerIds = new HashSet<>();
    for (var shipment : shipments) {
      if (shipment.from() != null) nodeIds.add(shipment.from().id());
      if (shipment.to() != null) nodeIds.add(shipment.to().id());
      if (shipment.deliveryManager() != null) managerIds.add(shipment.deliveryManager().id());
    }
    checkReadAccess(user, nodeIds, managerIds);
  }

  public static void checkCancelAccess(UserContext user, Delivery delivery) {
    Set<UUID> nodeIds = nodeIdsFrom(delivery.getShipments());
    switch (user.role()) {
      case MASTER -> {}
      case HUB_MANAGER -> {
        if (user.hubId() == null || !nodeIds.contains(user.hubId()))
          throw new BusinessException(CommonErrorCode.FORBIDDEN);
      }
      case VENDOR -> {
        Shipment last = delivery.getShipments().getLast();
        if (user.vendorId() == null || !user.vendorId().equals(last.getTo().getId()))
          throw new BusinessException(CommonErrorCode.FORBIDDEN);
      }
      default -> throw new BusinessException(CommonErrorCode.FORBIDDEN);
    }
  }

  public static void checkDeleteAccess(UserContext user, Delivery delivery) {
    switch (user.role()) {
      case MASTER -> {}
      case HUB_MANAGER -> {
        Shipment first = delivery.getShipments().getFirst();
        if (user.hubId() == null || !user.hubId().equals(first.getFrom().getId()))
          throw new BusinessException(CommonErrorCode.FORBIDDEN);
      }
      default -> throw new BusinessException(CommonErrorCode.FORBIDDEN);
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

  public static void checkShipmentStatusUpdateAccess(UserContext user, Shipment shipment) {
    switch (user.role()) {
      case MASTER -> {}
      case DELIVERY_MANAGER -> {
        if (shipment.getDeliveryManager() == null
            || !shipment.getDeliveryManager().getId().equals(user.userId()))
          throw new BusinessException(CommonErrorCode.FORBIDDEN);
      }
      default -> throw new BusinessException(CommonErrorCode.FORBIDDEN);
    }
  }

  private static Set<UUID> nodeIdsFrom(List<Shipment> shipments) {
    Set<UUID> nodeIds = new HashSet<>();
    for (var s : shipments) {
      if (s.getFrom() != null) nodeIds.add(s.getFrom().getId());
      if (s.getTo() != null) nodeIds.add(s.getTo().getId());
    }
    return nodeIds;
  }

  private static void checkReadAccess(UserContext user, Set<UUID> nodeIds, Set<UUID> managerIds) {
    switch (user.role()) {
      case MASTER, VENDOR -> {}
      case DELIVERY_MANAGER -> {
        if (!managerIds.contains(user.userId()))
          throw new BusinessException(CommonErrorCode.FORBIDDEN);
      }
      case HUB_MANAGER -> {
        if (!nodeIds.contains(user.hubId())) throw new BusinessException(CommonErrorCode.FORBIDDEN);
      }
      default -> throw new BusinessException(CommonErrorCode.FORBIDDEN);
    }
  }
}
