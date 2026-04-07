package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.delivery_server.delivery.domain.*;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryManagerNotFoundException;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import com.followMe.delivery_server.delivery.domain.service.DeliveryManagerAssigner;
import com.followMe.delivery_server.delivery.domain.service.DeliveryManagerSelector;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryManagerAssignerImpl implements DeliveryManagerAssigner {

  private final UserClient userClient;
  private final DeliveryRepository deliveryRepository;
  private final DeliveryManagerSelector selector;

  @Override
  public DeliveryManager assignDeliveryManager(UUID hubId, NodeType type) {
    List<DeliveryManagerInfo> managerInfos = userClient.getDeliveryManagers(hubId, type);
    List<UUID> managerIds = managerInfos.stream().map(DeliveryManagerInfo::userId).toList();

    Map<UUID, Integer> assignedCounts =
        deliveryRepository.countByDeliveryManagerIds(managerIds, ShipmentStatus.done());
    List<DeliveryManagerCandidate> candidates =
        managerInfos.stream()
            .map(
                info ->
                    new DeliveryManagerCandidate(
                        info.userId(),
                        info.name(),
                        info.sequence(),
                        assignedCounts.getOrDefault(info.userId(), 0)))
            .toList();

    DeliveryManagerInfo selected = selector.select(candidates);
    return DeliveryManager.of(selected.userId(), selected.name());
  }

  @Override
  public DeliveryManager getDeliveryManager(UUID userId, UUID hubId) {
    UserInfo userInfo = userClient.getUserInfo(userId);
    if (!userInfo.hubId().equals(hubId) || !userInfo.role().equals(UserRole.DELIVERY))
      throw new DeliveryManagerNotFoundException();
    return DeliveryManager.of(userId, userInfo.name());
  }
}
