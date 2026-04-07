package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record StatusResponse(
    UUID deliveryId, DeliveryStatus deliveryStatus, List<ShipmentSummary> shipments) {
  public static StatusResponse from(Delivery delivery) {
    List<ShipmentSummary> shipments =
        delivery.getShipments().stream()
            .map(
                s ->
                    new ShipmentSummary(
                        s.getId(),
                        new DeliveryResponse.NodeInfo(
                            s.getFrom().getId(), s.getFrom().getType(), s.getFrom().getName()),
                        new DeliveryResponse.NodeInfo(
                            s.getTo().getId(), s.getTo().getType(), s.getTo().getName()),
                        s.getStatus(),
                        s.getSequence(),
                        s.getDeliveryManager() != null ? s.getDeliveryManager().getId() : null,
                        s.getEstimatedDistance(),
                        s.getEstimatedDuration(),
                        s.getActualDistance(),
                        s.getActualDuration()))
            .toList();
    return new StatusResponse(delivery.getId(), delivery.getDeliveryStatus(), shipments);
  }

  public record ShipmentSummary(
      UUID shipmentId,
      DeliveryResponse.NodeInfo origin,
      DeliveryResponse.NodeInfo destination,
      ShipmentStatus status,
      int sequence,
      UUID deliveryManagerId,
      BigDecimal estimatedDistance,
      BigDecimal estimatedDuration,
      BigDecimal actualDistance,
      BigDecimal actualDuration) {}
}
