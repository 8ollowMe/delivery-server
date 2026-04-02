package com.followMe.delivery_server.delivery.application;

import static com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker.*;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryCreateResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Node;
import com.followMe.delivery_server.delivery.domain.UserContext;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryNotFoundException;
import com.followMe.delivery_server.delivery.domain.query.DeliveryQueryPort;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker;
import com.followMe.delivery_server.delivery.domain.service.HubRouteInfo;
import com.followMe.delivery_server.delivery.presentation.dto.DeliveryRequest;
import com.followMe.delivery_server.delivery.presentation.dto.DeliveryResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl {

  private final DeliveryRepository deliveryRepository;
  private final DeliveryQueryPort deliveryQueryPort;
  private final HubRouteInfo hubRouteInfo;
  private final DeliveryPermissionChecker permissionChecker;

  @Transactional
  public DeliveryCreateResponse createDelivery(DeliveryRequest.Create command) {
    List<Node> nodes = hubRouteInfo.getRouteNodes(command.sourceHubId(), command.vendorId());
    Delivery delivery = Delivery.create(command.orderId(), nodes);
    deliveryRepository.save(delivery);
    return new DeliveryCreateResponse(delivery.getId());
  }

  @Transactional(readOnly = true)
  public DeliveryResponse.Detail getDelivery(UserContext user, UUID deliveryId) {
    DeliveryResponse.Detail detail = deliveryQueryPort.findDeliveryById(deliveryId);
    permissionChecker.checkReadAccess(user, detail.shipments());
    return detail;
  }

  @Transactional(readOnly = true)
  public PageResponse<DeliveryResponse.ListItem> getDeliveries(
      UserContext user, PageRequest pageRequest, DeliverySearchCondition condition) {
    permissionChecker.checkListAccess(user, condition);
    return deliveryQueryPort.findDeliveries(pageRequest, condition);
  }

  @Transactional(readOnly = true)
  public DeliveryResponse.Detail getDeliveryByOrderId(UserContext user, UUID orderId) {
    DeliveryResponse.Detail detail = deliveryQueryPort.findDeliveryByOrderId(orderId);
    permissionChecker.checkReadAccess(user, detail.shipments());
    return detail;
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
    delivery.softDelete(user,permissionChecker);
  }
}
