package com.followMe.delivery_server.delivery.application;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryCreateResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.application.dto.OrderCreateCommand;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Node;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import com.followMe.delivery_server.delivery.infra.DeliveryQueryRepository;
import com.followMe.delivery_server.delivery.infra.hub.HubClient;
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
  public DeliveryResponseDto getDelivery(UUID deliveryId) {
    return deliveryQueryRepository.findDeliveryById(deliveryId);
  }

  @Override
  @Transactional(readOnly = true)
  public PageResponse<DeliveryResponseDto> getDeliveries(
      PageRequest pageRequest, DeliverySearchCondition condition) {
    return deliveryQueryRepository.findDeliveries(pageRequest, condition);
  }
}
