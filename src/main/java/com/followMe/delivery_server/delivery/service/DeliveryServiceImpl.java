package com.followMe.delivery_server.delivery.service;

import com.followMe.delivery_server.client.HubClient;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Node;
import com.followMe.delivery_server.delivery.dto.OrderCreateCommand;
import com.followMe.delivery_server.delivery.repository.DeliveryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

  private final DeliveryRepository deliveryRepository;
  private final HubClient hubClient;

  @Transactional
  @Override
  public void createDelivery(OrderCreateCommand command) {

    List<Node> nodes = hubClient.getNodes(command.sourceHubId(), command.vendorId()).toDomain();
    Delivery delivery = Delivery.create(command.orderId(), nodes);
    deliveryRepository.save(delivery);
  }
}
