package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import java.time.LocalDateTime;
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

  public record ManagerInfo(UUID id, String name) {}
	public record DeliveryCreate(UUID deliveryId, List<NodeInfo> nodes, ManagerInfo managerInfo) {

		public static DeliveryCreate of(Delivery delivery, DeliveryManager manager) {
			List<NodeInfo> nodeInfoList = delivery.getShipments()
					.stream()
					.map(shipment -> new NodeInfo(shipment.getTo()
							.getId(), shipment.getTo()
							.getType(), shipment.getTo()
							.getName()))
					.toList();

			ManagerInfo managerInfo = new ManagerInfo(manager.getId(), manager.getName());

			return new DeliveryCreate(delivery.getId(), nodeInfoList, managerInfo);

		}
	}
}
