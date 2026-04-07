package com.followMe.delivery_server.delivery.domain.service;

import java.util.UUID;

public interface DeliverySequenceUpdater {
  void updateSequence(UUID managerId);
}