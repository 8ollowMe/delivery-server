package com.followMe.delivery_server.delivery.application;

import com.followMe.delivery_server.delivery.infra.hub.HubClient;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Node;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.OrderCreateCommand;
import com.followMe.delivery_server.delivery.infra.DeliveryQueryRepository;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
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
  public void createDelivery(OrderCreateCommand command) {

    List<Node> nodes = hubClient.getNodes(command.sourceHubId(), command.vendorId()).toDomain();
    Delivery delivery = Delivery.create(command.orderId(), nodes);
    deliveryRepository.save(delivery);
  }

  @Override
  @Transactional(readOnly = true)
  public DeliveryResponseDto getDelivery(UUID deliveryId) {
    return deliveryQueryRepository.findDeliveryById(deliveryId);
  }
}
