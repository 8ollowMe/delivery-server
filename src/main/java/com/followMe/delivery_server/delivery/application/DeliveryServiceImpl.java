package com.followMe.delivery_server.delivery.application;

import static com.followMe.delivery_server.delivery.application.DeliveryPermissionChecker.checkAccess;
import static com.followMe.delivery_server.delivery.application.DeliveryPermissionChecker.checkListAccess;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryCreateResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.application.dto.OrderCreateCommand;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Node;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import com.followMe.delivery_server.delivery.infra.hub.HubClient;
import com.followMe.delivery_server.delivery.infra.query.DeliveryQueryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

  private final DeliveryRepository deliveryRepository;
  private final DeliveryQueryRepository deliveryQueryRepository;
  private final HubClient hubClient;

  @Transactional
  @Override
  public DeliveryCreateResponse createDelivery(OrderCreateCommand command) {

    List<Node> nodes = hubClient.getNodes(command.sourceHubId(), command.vendorId()).toDomain();
    Delivery delivery = Delivery.create(command.orderId(), nodes);
    deliveryRepository.save(delivery);
    return new DeliveryCreateResponse(delivery.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public DeliveryResponseDto getDelivery(UserContext user, UUID deliveryId) {
    DeliveryResponseDto deliveryResponseDto = deliveryQueryRepository.findDeliveryById(deliveryId);
    checkAccess(user, deliveryResponseDto);
    return deliveryResponseDto;
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<DeliveryListItemDto> getDeliveries(
      UserContext user, PageRequest pageRequest, DeliverySearchCondition condition) {

    checkListAccess(user, condition);
    return deliveryQueryRepository.findDeliveries(pageRequest, condition);
  }

  @Override
  public DeliveryResponseDto getDeliveryByOrderId(UserContext user, UUID orderId) {
    DeliveryResponseDto deliveryResponseDto =
        deliveryQueryRepository.findDeliveryByOrderId(orderId);
    checkAccess(user, deliveryResponseDto);
    return deliveryResponseDto;
  }
}
