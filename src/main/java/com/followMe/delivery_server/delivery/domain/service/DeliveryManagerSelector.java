package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.delivery_server.delivery.domain.DeliveryManagerCandidate;
import com.followMe.delivery_server.delivery.domain.DeliveryManagerInfo;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryManagerAssignException;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DeliveryManagerSelector {
  public DeliveryManagerInfo select(List<DeliveryManagerCandidate> candidates) {
    return candidates.stream()
        .min(
            Comparator.comparingInt(DeliveryManagerCandidate::assignedCount)
                .thenComparingLong(DeliveryManagerCandidate::sequence))
        .map(
            candidate ->
                new DeliveryManagerInfo(candidate.id(), candidate.name(), candidate.sequence()))
        .orElseThrow(DeliveryManagerAssignException::new);
  }
}
