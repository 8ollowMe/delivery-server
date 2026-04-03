package com.followMe.delivery_server.delivery.application;

import com.followMe.delivery_server.delivery.application.dto.ShipmentResponse;
import com.followMe.delivery_server.delivery.domain.Delivery;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.UserContext;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryNotFoundException;
import com.followMe.delivery_server.delivery.domain.exception.ShipmentNotFoundException;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentService {

  private final DeliveryRepository deliveryRepository;
  private final DeliveryQueryPort deliveryQueryPort;
  private final DeliveryPermissionChecker permissionChecker;

  @Transactional(readOnly = true)
  public List<ShipmentResponse.Detail> getShipmentsByDeliveryId(UserContext user, UUID deliveryId) {
    Delivery delivery =
        deliveryRepository.findById(deliveryId).orElseThrow(DeliveryNotFoundException::new);
    permissionChecker.checkReadAccess(user, delivery.getShipments());
    return deliveryQueryPort.findShipmentsByDeliveryId(deliveryId);
  }

  @Transactional(readOnly = true)
  public ShipmentResponse.Detail getShipment(UserContext user, UUID shipmentId) {
    Shipment shipment =
        deliveryRepository.findShipmentById(shipmentId).orElseThrow(ShipmentNotFoundException::new);
    permissionChecker.checkReadAccess(user, List.of(shipment));
    return deliveryQueryPort.findShipmentById(shipmentId);
  }

  @Transactional
  public void updateStatus(UserContext user, UUID shipmentId, ShipmentStatus status) {
    Shipment shipment =
        deliveryRepository.findShipmentById(shipmentId).orElseThrow(ShipmentNotFoundException::new);
    shipment.updateShipmentStatus(status, user, permissionChecker);
  }
}
