package com.followMe.delivery_server.delivery.application;

import com.followMe.common.exception.BusinessException;
import com.followMe.common.exception.CommonErrorCode;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.UserContext;
import com.followMe.delivery_server.delivery.domain.exception.ForbiddenException;
import com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DeliveryPermissionCheckerImpl implements DeliveryPermissionChecker {

  private static Set<UUID> nodeIdsFrom(List<Shipment> shipments) {
    Set<UUID> nodeIds = new HashSet<>();
    for (var s : shipments) {
      if (s.getFrom() != null) nodeIds.add(s.getFrom().getId());
      if (s.getTo() != null) nodeIds.add(s.getTo().getId());
    }
    return nodeIds;
  }

  @Override
  public void checkCancelAccess(UserContext user, Delivery delivery) {
    Set<UUID> nodeIds = nodeIdsFrom(delivery.getShipments());
    switch (user.role()) {
      case MASTER -> {}
      case HUB -> {
        if (user.hubId() == null || !nodeIds.contains(user.hubId()))
          throw new ForbiddenException();
      }
      case VENDOR -> {
        Shipment last = delivery.getShipments().getLast();
        if (user.vendorId() == null || !user.vendorId().equals(last.getTo().getId()))
          throw new ForbiddenException();
      }
      default -> throw new ForbiddenException();
    }
  }

  @Override
  public void checkDeleteAccess(UserContext user, Delivery delivery) {
    switch (user.role()) {
      case MASTER -> {}
      case HUB -> {
        Shipment first = delivery.getShipments().getFirst();
        if (user.hubId() == null || !user.hubId().equals(first.getFrom().getId()))
          throw new ForbiddenException();
      }
      default -> throw new ForbiddenException();
    }
  }

  @Override
  public void checkShipmentStatusUpdateAccess(UserContext user, Shipment shipment) {
    switch (user.role()) {
      case MASTER -> {}
      case DELIVERY -> {
        if (shipment.getDeliveryManager() == null
            || !shipment.getDeliveryManager().getId().equals(user.userId()))
          throw new ForbiddenException();
      }
      default -> throw new ForbiddenException();
    }
  }

  @Override
  public void checkShipmentManagerReassignAccess(UserContext user, Shipment shipment) {
    switch (user.role()) {
      case MASTER -> {}
      case HUB -> {
        if (user.hubId() == null || !user.hubId().equals(shipment.getFrom().getId()))
          throw new ForbiddenException();
      }
      default -> throw new ForbiddenException();
    }
  }
}
