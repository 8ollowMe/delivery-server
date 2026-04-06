package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.DeliveryManager;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeliveryResponse {

  public record Detail(
      UUID id,
      UUID orderId,
      DeliveryStatus status,
      List<ShipmentResponse.Detail> shipments,
      LocalDateTime createdAt) {}

  public record ListItem(
      UUID id,
      UUID orderId,
      DeliveryStatus status,
      NodeInfo fromHub,
      NodeInfo toVendor,
      int totalShipments,
      int completedShipments,
      NodeInfo latestProgressedNode,
      ShipmentStatus currentProgressStatus,
      LocalDateTime createdAt) {}

  public record NodeInfo(UUID id, NodeType type, String name) {}

  public record ManagerInfo(UUID id, String name) {
    public static ManagerInfo of(DeliveryManager deliveryManager) {
      return new ManagerInfo(deliveryManager.getId(), deliveryManager.getName());
    }
  }

  public record DeliveryCreate(
      UUID deliveryId, List<String> waypoints, UUID deliveryManagerId, String deliveryManagerName) {

    public static DeliveryCreate of(Delivery delivery, DeliveryManager manager) {
      List<String> waypoints = new ArrayList<>();
      for (Shipment shipment : delivery.getShipments()) {
        if (shipment.getTo().getType() != NodeType.VENDOR)
          waypoints.add(shipment.getTo().getName());
      }

      return new DeliveryCreate(delivery.getId(), waypoints, manager.getId(), manager.getName());
    }
  }
}
