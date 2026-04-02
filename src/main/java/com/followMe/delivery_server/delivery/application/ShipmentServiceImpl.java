package com.followMe.delivery_server.delivery.application;

import static com.followMe.delivery_server.delivery.application.DeliveryPermissionChecker.checkReadAccess;
import static com.followMe.delivery_server.delivery.application.DeliveryPermissionChecker.checkShipmentStatusUpdateAccess;

import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.domain.Shipment;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryException;
import com.followMe.delivery_server.delivery.domain.repository.DeliveryRepository;
import com.followMe.delivery_server.delivery.infra.query.DeliveryQueryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

  private final DeliveryQueryRepository deliveryQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public List<DeliveryResponseDto.ShipmentResponse> getShipmentsByDeliveryId(
      UserContext user, UUID deliveryId) {
    List<DeliveryResponseDto.ShipmentResponse> shipmentDtoList =
        deliveryQueryRepository.findShipmentsByDeliveryId(deliveryId);
    checkReadAccess(user, shipmentDtoList);
    return shipmentDtoList;
  }
}
