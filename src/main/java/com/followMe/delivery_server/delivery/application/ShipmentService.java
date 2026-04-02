package com.followMe.delivery_server.delivery.application;

import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.UserContext;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryNotFoundException;
import com.followMe.delivery_server.delivery.domain.exception.ShipmentNotFoundException;
import com.followMe.delivery_server.delivery.domain.query.DeliveryQueryPort;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker;
import com.followMe.delivery_server.delivery.presentation.dto.ShipmentResponse;
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
    deliveryRepository.findById(deliveryId).orElseThrow(DeliveryNotFoundException::new);
    List<ShipmentResponse.Detail> shipmentDtoList =
        deliveryQueryPort.findShipmentsByDeliveryId(deliveryId);
    permissionChecker.checkReadAccess(user, shipmentDtoList);
    return shipmentDtoList;
  }

  @Transactional(readOnly = true)
  public ShipmentResponse.Detail getShipment(UserContext user, UUID shipmentId) {
    ShipmentResponse.Detail shipment = deliveryQueryPort.findShipmentById(shipmentId);
    permissionChecker.checkReadAccess(user, List.of(shipment));
    return shipment;
  }

  @Transactional
  public void updateStatus(UserContext user, UUID shipmentId, ShipmentStatus status) {
    Shipment shipment =
        deliveryRepository.findShipmentById(shipmentId).orElseThrow(ShipmentNotFoundException::new);
    shipment.updateShipmentStatus(status, user, permissionChecker);
  }
}
