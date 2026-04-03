package com.followMe.delivery_server.delivery.application;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryRequest;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponse;
import com.followMe.delivery_server.delivery.domain.*;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryNotFoundException;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import com.followMe.delivery_server.delivery.domain.service.DeliveryManagerAssigner;
import com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker;
import com.followMe.delivery_server.delivery.domain.service.HubRouteInfo;
import com.followMe.delivery_server.delivery.domain.service.OrderDeliveryAssigner;
import com.followMe.delivery_server.delivery.infra.client.OrderClient;
import com.followMe.delivery_server.delivery.infra.client.UserClient;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {

  private final DeliveryRepository deliveryRepository;
  private final HubRouteInfo hubRouteInfo;
  private final DeliveryManagerAssigner assigner;
  private final DeliveryQueryPort deliveryQueryPort;
  private final DeliveryPermissionChecker permissionChecker;
  private final UserClient userClient;
  private final OrderClient orderClient;

  @Transactional
  public DeliveryResponse.DeliveryCreate createDelivery(DeliveryRequest.Create command) {
    List<Node> nodes = hubRouteInfo.getRouteNodes(command.sourceHubId(), command.vendorId());
    Delivery delivery = Delivery.create(command.orderId(), nodes);
    Shipment shipment = delivery.getShipments().getFirst();
    NodeType type = shipment.getTo().getType();
    DeliveryManager manager = assigner.assignDeliveryManager(command.sourceHubId(), type);
    shipment.assignDeliveryManager(manager, userClient, orderClient);
    deliveryRepository.save(delivery);
    return DeliveryResponse.DeliveryCreate.of(delivery, manager);
  }

  @Transactional(readOnly = true)
  public DeliveryResponse.Detail getDelivery(UserContext user, UUID deliveryId) {
    Delivery delivery =
        deliveryRepository.findById(deliveryId).orElseThrow(DeliveryNotFoundException::new);
    delivery.checkReadAccess(user);
    return deliveryQueryPort.findDeliveryById(deliveryId);
  }

  @Transactional(readOnly = true)
  public PageResponse<DeliveryResponse.ListItem> getDeliveries(
      UserContext user, PageRequest pageRequest, DeliverySearchCondition condition) {
    condition.applyPermission(user);
    return deliveryQueryPort.findDeliveries(pageRequest, condition);
  }

  @Transactional(readOnly = true)
  public DeliveryResponse.Detail getDeliveryByOrderId(UserContext user, UUID orderId) {
    Delivery delivery =
        deliveryRepository.findByOrderId(orderId).orElseThrow(DeliveryNotFoundException::new);
    delivery.checkReadAccess(user);
    return deliveryQueryPort.findDeliveryByOrderId(orderId);
  }

  @Transactional
  public void cancelDelivery(UserContext user, UUID deliveryId) {
    Delivery delivery =
        deliveryRepository.findById(deliveryId).orElseThrow(DeliveryNotFoundException::new);
    delivery.cancel(user, permissionChecker);
  }

  @Transactional
  public void deleteDelivery(UserContext user, UUID deliveryId) {
    Delivery delivery =
        deliveryRepository.findById(deliveryId).orElseThrow(DeliveryNotFoundException::new);
    delivery.softDelete(user, permissionChecker);
  }
}
