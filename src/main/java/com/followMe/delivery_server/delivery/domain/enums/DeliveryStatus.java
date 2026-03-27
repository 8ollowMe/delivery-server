package com.followMe.delivery_server.delivery.domain.enums;

import java.util.Set;

public enum DeliveryStatus {
  READY,
  IN_PROGRESS,
  COMPLETED,
  FAILED,
  CANCELLED;

  private Set<DeliveryStatus> allowedTransitions;

  static {
    READY.allowedTransitions = Set.of(IN_PROGRESS, CANCELLED);
    IN_PROGRESS.allowedTransitions = Set.of(COMPLETED, FAILED);
    COMPLETED.allowedTransitions = Set.of();
    FAILED.allowedTransitions = Set.of();
    CANCELLED.allowedTransitions = Set.of();
  }

  public boolean isTransitionNotAllowed(DeliveryStatus next) {
    return !allowedTransitions.contains(next);
  }

  public boolean fixed() {
    return allowedTransitions.isEmpty();
  }
}
