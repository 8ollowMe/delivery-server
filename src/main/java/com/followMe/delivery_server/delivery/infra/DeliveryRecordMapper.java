package com.followMe.delivery_server.delivery.infra;

import static com.followMe.delivery_server.jooq.tables.PDelivery.P_DELIVERY;
import static com.followMe.delivery_server.jooq.tables.PShipment.P_SHIPMENT;

import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto.DeliveryManagerResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto.NodeResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto.ShipmentResponse;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import java.util.List;
import java.util.stream.Stream;
import org.jooq.Record;
import org.springframework.stereotype.Component;

@Component
public class DeliveryRecordMapper {

  public DeliveryResponseDto toDeliveryResponse(Record first, Stream<Record> records) {
    List<ShipmentResponse> shipments =
        records.filter(r -> r.get(P_SHIPMENT.ID) != null).map(this::toShipmentResponse).toList();

    return new DeliveryResponseDto(
        first.get(P_DELIVERY.ID),
        first.get(P_DELIVERY.ORDER_ID),
        DeliveryStatus.valueOf(first.get(P_DELIVERY.STATUS)),
        shipments);
  }

  private ShipmentResponse toShipmentResponse(Record r) {
    return new ShipmentResponse(
        r.get(P_SHIPMENT.ID),
        r.get(P_SHIPMENT.SEQUENCE),
        ShipmentStatus.valueOf(r.get(P_SHIPMENT.STATUS)),
        ShipmentType.valueOf(r.get(P_SHIPMENT.TYPE)),
        new NodeResponse(
            r.get(P_SHIPMENT.FROM_NODE_ID),
            NodeType.valueOf(r.get(P_SHIPMENT.FROM_NODE_TYPE)),
            r.get(P_SHIPMENT.FROM_NODE_NAME)),
        new NodeResponse(
            r.get(P_SHIPMENT.TO_NODE_ID),
            NodeType.valueOf(r.get(P_SHIPMENT.TO_NODE_TYPE)),
            r.get(P_SHIPMENT.TO_NODE_NAME)),
        new DeliveryManagerResponse(
            r.get(P_SHIPMENT.DELIVERY_MANAGER_ID), r.get(P_SHIPMENT.DELIVERY_MANAGER_NAME)),
        r.get(P_SHIPMENT.SHIPPED_AT) != null
            ? r.get(P_SHIPMENT.SHIPPED_AT).toLocalDateTime()
            : null,
        r.get(P_SHIPMENT.ARRIVED_AT) != null
            ? r.get(P_SHIPMENT.ARRIVED_AT).toLocalDateTime()
            : null,
        r.get(P_SHIPMENT.COMPLETED_AT) != null
            ? r.get(P_SHIPMENT.COMPLETED_AT).toLocalDateTime()
            : null);
  }
}
