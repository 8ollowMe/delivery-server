package com.followMe.delivery_server.delivery.application;

import static com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker.checkReadAccess;
import static com.followMe.delivery_server.delivery.domain.service.DeliveryPermissionChecker.checkShipmentStatusUpdateAccess;

import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.UserContext;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.exception.ShipmentNotFoundException;
import com.followMe.delivery_server.delivery.domain.query.DeliveryQueryPort;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl {

  private final DeliveryRepository deliveryRepository;
  private final DeliveryQueryPort deliveryQueryPort;

  @Transactional(readOnly = true)
  public List<DeliveryResponseDto.ShipmentResponse> getShipmentsByDeliveryId(
      UserContext user, UUID deliveryId) {
    deliveryRepository
        .findById(deliveryId)
        .orElseThrow(DeliveryException.DeliveryNotFoundException::new);
    List<DeliveryResponseDto.ShipmentResponse> shipmentDtoList =
        deliveryQueryRepository.findShipmentsByDeliveryId(deliveryId);
    checkReadAccess(user, shipmentDtoList);
    return shipmentDtoList;
  }

  @Transactional(readOnly = true)
  public DeliveryResponseDto.ShipmentResponse getShipment(UserContext user, UUID shipmentId) {
    DeliveryResponseDto.ShipmentResponse shipment = deliveryQueryPort.findShipmentById(shipmentId);
    checkReadAccess(user, List.of(shipment));
    return shipment;
  }

  @Transactional
  public void updateStatus(UserContext user, UUID shipmentId, ShipmentStatus status) {
    Shipment shipment =
        deliveryRepository.findShipmentById(shipmentId).orElseThrow(ShipmentNotFoundException::new);
    checkShipmentStatusUpdateAccess(user, shipment);
    shipment.updateShipmentStatus(status);
  }
}
