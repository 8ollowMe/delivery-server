package com.followMe.delivery_server.delivery.infra.event;

import com.followMe.common.event.outbox.OutboxEvent;
import com.followMe.delivery_server.delivery.application.ShipmentService;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.event.DeliveryEvents;
import com.followMe.delivery_server.delivery.domain.event.ShipmentCompleted;
import com.followMe.delivery_server.delivery.domain.exception.ShipmentNotFoundException;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentCompletedHandler {

  private final DeliveryRepository deliveryRepository;
  private final ShipmentService shipmentService;
  private final DeliveryEvents deliveryEvents;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handle(OutboxEvent outboxEvent) {
    if (!(outboxEvent.event() instanceof ShipmentCompleted completed)) return;

    ShipmentCompleted.Payload payload = (ShipmentCompleted.Payload) completed.getPayload();
    UUID deliveryId = payload.deliveryId();
    UUID completedShipmentId = payload.shipmentId();

    Delivery delivery = deliveryRepository.findById(deliveryId).orElse(null);
    if (delivery == null) return;
    Shipment completedShipment =
        Objects.requireNonNull(getTargetShipment(delivery, completedShipmentId))
            .orElseThrow(ShipmentNotFoundException::new);

    int nextSequence = completedShipment.getSequence() + 1;

    Shipment nextShipment =
        delivery.getShipments().stream()
            .filter(shipment -> shipment.getSequence() == nextSequence)
            .findFirst()
            .orElse(null);
    if (nextShipment == null) return;
    shipmentService.assignManagerForInternal(nextShipment.getId());
  }

  private static Optional<Shipment> getTargetShipment(Delivery delivery, UUID completedShipmentId) {
    Shipment targetShipment;
    for (Shipment shipment : delivery.getShipments()) {
      if (shipment.getId().equals(completedShipmentId)) {
        targetShipment = shipment;
        return Optional.of(targetShipment);
      }
    }
    return Optional.empty();
  }
}
