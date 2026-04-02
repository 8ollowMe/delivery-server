package com.followMe.delivery_server.delivery.infra.query;

import static com.followMe.delivery_server.jooq.tables.PDelivery.P_DELIVERY;
import static com.followMe.delivery_server.jooq.tables.PShipment.P_SHIPMENT;

import com.followMe.delivery_server.delivery.application.dto.DeliveryListItemDto;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponse.NodeResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto.DeliveryManagerResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto.ShipmentResponse;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import java.util.List;
import java.util.stream.Stream;
import org.jooq.Record;
import org.jooq.Result;
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
        shipments,
        first.get(P_DELIVERY.CREATED_AT).toLocalDateTime());
  }

  public DeliveryListItemDto toListItemDto(Record first, Result<Record> group) {
    List<Record> shipments = group.stream().filter(r -> r.get(P_SHIPMENT.ID) != null).toList();

    if (shipments.isEmpty()) {
      return new DeliveryListItemDto(
          first.get(P_DELIVERY.ID),
          first.get(P_DELIVERY.ORDER_ID),
          DeliveryStatus.valueOf(first.get(P_DELIVERY.STATUS)),
          null,
          null,
          0,
          0,
          null,
          null,
          first.get(P_DELIVERY.CREATED_AT).toLocalDateTime());
    }
    int totalShipments = shipments.size();
    int completedShipments = 0;

    NodeResponse fromHub = null;
    NodeResponse toVendor = null;

    NodeResponse latestProgressedNode = null;
    ShipmentStatus lastShipmentStatus = null;
    for (Record shipment : shipments) {
      if (shipment.get(P_SHIPMENT.SEQUENCE).equals(1)) {
        fromHub = fromNodeOf(shipment);
      }
      if (shipment.get(P_SHIPMENT.SEQUENCE).equals(totalShipments)) {
        toVendor = toNodeOf(shipment);
      }

      ShipmentStatus status = ShipmentStatus.valueOf(shipment.get(P_SHIPMENT.STATUS));
      if (status == ShipmentStatus.COMPLETED) {
        completedShipments++;
        latestProgressedNode = toNodeOf(shipment);
        lastShipmentStatus = status;
      } else if (ShipmentStatus.isInProgress(status)) {
        latestProgressedNode = toNodeOf(shipment);
        lastShipmentStatus = status;
      }
    }

    return new DeliveryListItemDto(
        first.get(P_DELIVERY.ID),
        first.get(P_DELIVERY.ORDER_ID),
        DeliveryStatus.valueOf(first.get(P_DELIVERY.STATUS)),
        fromHub,
        toVendor,
        totalShipments,
        completedShipments,
        latestProgressedNode,
        lastShipmentStatus,
        first.get(P_DELIVERY.CREATED_AT).toLocalDateTime());
  }

  public List<ShipmentResponse> toShipmentResponseList(Result<Record> records) {
    return records.map(this::toShipmentResponse);
  }

  protected ShipmentResponse toShipmentResponse(Record record) {
    return new ShipmentResponse(
        record.get(P_SHIPMENT.ID),
        record.get(P_SHIPMENT.SEQUENCE),
        ShipmentStatus.valueOf(record.get(P_SHIPMENT.STATUS)),
        ShipmentType.valueOf(record.get(P_SHIPMENT.TYPE)),
        fromNodeOf(record),
        toNodeOf(record),
        record.get(P_SHIPMENT.DELIVERY_MANAGER_ID) != null
            ? new DeliveryManagerResponse(
                record.get(P_SHIPMENT.DELIVERY_MANAGER_ID),
                record.get(P_SHIPMENT.DELIVERY_MANAGER_NAME))
            : null,
        record.get(P_SHIPMENT.SHIPPED_AT) != null
            ? record.get(P_SHIPMENT.SHIPPED_AT).toLocalDateTime()
            : null,
        record.get(P_SHIPMENT.ARRIVED_AT) != null
            ? record.get(P_SHIPMENT.ARRIVED_AT).toLocalDateTime()
            : null,
        record.get(P_SHIPMENT.COMPLETED_AT) != null
            ? record.get(P_SHIPMENT.COMPLETED_AT).toLocalDateTime()
            : null);
  }

  private NodeResponse fromNodeOf(Record record) {
    return new NodeResponse(
        record.get(P_SHIPMENT.FROM_NODE_ID),
        NodeType.valueOf(record.get(P_SHIPMENT.FROM_NODE_TYPE)),
        record.get(P_SHIPMENT.FROM_NODE_NAME));
  }

  private NodeResponse toNodeOf(Record record) {
    return new NodeResponse(
        record.get(P_SHIPMENT.TO_NODE_ID),
        NodeType.valueOf(record.get(P_SHIPMENT.TO_NODE_TYPE)),
        record.get(P_SHIPMENT.TO_NODE_NAME));
  }
}
