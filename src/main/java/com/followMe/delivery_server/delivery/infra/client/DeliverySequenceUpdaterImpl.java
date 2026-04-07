package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.delivery_server.delivery.domain.service.DeliverySequenceUpdater;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliverySequenceUpdaterImpl implements DeliverySequenceUpdater {

  private final UserClient userClient;

  @Override
  public void updateSequence(UUID managerId) {
    userClient.updateDeliverySequence(managerId);
  }
}
