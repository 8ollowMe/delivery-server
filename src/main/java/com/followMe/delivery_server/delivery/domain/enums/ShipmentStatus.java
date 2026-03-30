package com.followMe.delivery_server.delivery.domain.enums;

import java.util.Set;

public enum ShipmentStatus {
  PENDING,
  SHIPPED,
  IN_TRANSIT,
  ARRIVED,
  COMPLETED,
  FAILED,
  CANCELLED;

  static {
    PENDING.allowedTransitions = Set.of(SHIPPED, CANCELLED);
    SHIPPED.allowedTransitions = Set.of(IN_TRANSIT, FAILED);
    IN_TRANSIT.allowedTransitions = Set.of(ARRIVED, FAILED);
    ARRIVED.allowedTransitions = Set.of(COMPLETED, FAILED);
    COMPLETED.allowedTransitions = Set.of();
    FAILED.allowedTransitions = Set.of();
    CANCELLED.allowedTransitions = Set.of();
  }

  private Set<ShipmentStatus> allowedTransitions;

  public boolean canTransitionTo(ShipmentStatus next) {
    return allowedTransitions.contains(next);
  }
}
