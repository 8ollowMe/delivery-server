package com.followMe.delivery_server.delivery.application;

import static com.followMe.delivery_server.delivery.application.UserRole.*;

import com.followMe.common.exception.BusinessException;
import com.followMe.common.exception.CommonErrorCode;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import java.util.*;

public class DeliveryPermissionChecker {

  public static void checkReadAccess(UserContext user, DeliveryResponseDto delivery) {
    ShipmentInfoIds shipmentInfoIds = getShipmentInfoIds(delivery);

    checkReadAccess(user, shipmentInfoIds.nodeIds, shipmentInfoIds.managerIds);
  }

  public static void checkCancelAccess(UserContext user, Delivery delivery) {
    ShipmentInfoIds shipmentInfoIds = getShipmentInfoIds(delivery.getShipments());
    switch (user.role()) {
      case MASTER -> {}
      case HUB_MANAGER -> {
        if (user.hubId() == null || !shipmentInfoIds.nodeIds.contains(user.hubId())) {
          throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
      }
      case VENDOR -> {
        Shipment lastShipment = delivery.getShipments().getLast();
        if (user.vendorId() == null || !user.vendorId().equals(lastShipment.getTo().getId())) {
          throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
      }
      default -> throw new BusinessException(CommonErrorCode.FORBIDDEN);
    }
  }

  public static void checkDeleteAccess(UserContext user, Delivery delivery) {
    switch (user.role()) {
      case MASTER -> {}
      case HUB_MANAGER -> {
        Shipment firstShipment = delivery.getShipments().getFirst();
        if (user.hubId() == null || !user.hubId().equals(firstShipment.getFrom().getId())) {
          throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
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

  private record ShipmentInfoIds(Set<UUID> nodeIds, Set<UUID> managerIds) {}

  private static ShipmentInfoIds getShipmentInfoIds(List<Shipment> shipments) {
    Set<UUID> nodeIds = new HashSet<>();
    Set<UUID> managerIds = new HashSet<>();

    for (var shipment : shipments) {
      if (shipment.getFrom() != null) nodeIds.add(shipment.getFrom().getId());
      if (shipment.getTo() != null) nodeIds.add(shipment.getTo().getId());
      if (shipment.getDeliveryManager() != null)
        managerIds.add(shipment.getDeliveryManager().getId());
    }

    return new ShipmentInfoIds(nodeIds, managerIds);
  }

  private static ShipmentInfoIds getShipmentInfoIds(DeliveryResponseDto delivery) {
    Set<UUID> nodeIds = new HashSet<>();
    Set<UUID> managerIds = new HashSet<>();

    for (var shipment : delivery.shipments()) {
      if (shipment.from() != null) nodeIds.add(shipment.from().id());
      if (shipment.to() != null) nodeIds.add(shipment.to().id());
      if (shipment.deliveryManager() != null) managerIds.add(shipment.deliveryManager().id());
    }

    return new ShipmentInfoIds(nodeIds, managerIds);
  }

  private static void checkReadAccess(
      UserContext user, Set<UUID> nodeIds, Set<UUID> deliveryManagerIds) {
    if (user.role() == MASTER || user.role() == VENDOR) return;
    if (user.role() == HUB_MANAGER) {
      checkIfDeliveryRelatedToUserHub(user.hubId(), nodeIds);
      return;
    }
    if (user.role() == DELIVERY_MANAGER) {
      checkIfAssignedToUser(user.userId(), deliveryManagerIds);
    }
  }

  private static void checkIfDeliveryRelatedToUserHub(UUID hubId, Set<UUID> nodeIds) {
    if (!nodeIds.contains(hubId)) {
      throw new BusinessException(CommonErrorCode.FORBIDDEN);
    }
  }

  private static void checkIfAssignedToUser(UUID userId, Set<UUID> managerIds) {
    if (!managerIds.contains(userId)) {
      throw new BusinessException(CommonErrorCode.FORBIDDEN);
    }
  }
}
