package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.DeliveryManager;

import java.util.List;
import java.util.UUID;

public record DeliveryCreateResponse(UUID deliveryId,
									 List<DeliveryResponse.NodeInfo> nodeInfoList,
									 DeliveryResponse.ManagerInfo currentDeliveryManager) {
	public static DeliveryCreateResponse of(Delivery delivery, DeliveryManager manager) {
		List<DeliveryResponse.NodeInfo> nodeInfoList = delivery.getShipments().stream()
				.map(shipment -> new DeliveryResponse.NodeInfo(
						shipment.getTo().getId(),
						shipment.getTo().getType(),
						shipment.getTo().getName()))
				.toList();

		DeliveryResponse.ManagerInfo managerInfo = new DeliveryResponse.ManagerInfo(
				manager.getId(), manager.getName());

		return new DeliveryCreateResponse(delivery.getId(), nodeInfoList, managerInfo);
	}


}
