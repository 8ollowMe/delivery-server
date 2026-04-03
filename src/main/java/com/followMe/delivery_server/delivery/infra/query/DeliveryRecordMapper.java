package com.followMe.delivery_server.delivery.infra.query;

import static com.followMe.delivery_server.jooq.tables.PDelivery.P_DELIVERY;
import static com.followMe.delivery_server.jooq.tables.PShipment.P_SHIPMENT;

import com.followMe.delivery_server.delivery.application.dto.DeliveryResponse;
import com.followMe.delivery_server.delivery.application.dto.ShipmentResponse;
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

  public DeliveryResponse.Detail toDetail(Record first, Stream<Record> records) {
    List<ShipmentResponse.Detail> shipments =
        records.filter(r -> r.get(P_SHIPMENT.ID) != null).map(this::toShipmentDetail).toList();

    return new DeliveryResponse.Detail(
        first.get(P_DELIVERY.ID),
        first.get(P_DELIVERY.ORDER_ID),
        deriveDeliveryStatus(shipments.stream().map(ShipmentResponse.Detail::status).toList()),
        shipments,
        first.get(P_DELIVERY.CREATED_AT).toLocalDateTime());
  }

  public DeliveryResponse.ListItem toListItem(Record first, Result<Record> group) {
    List<Record> shipments = group.stream().filter(r -> r.get(P_SHIPMENT.ID) != null).toList();

    if (shipments.isEmpty()) {
      return new DeliveryResponse.ListItem(
          first.get(P_DELIVERY.ID),
          first.get(P_DELIVERY.ORDER_ID),
          DeliveryStatus.READY,
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
    DeliveryResponse.NodeInfo fromHub = null;
    DeliveryResponse.NodeInfo toVendor = null;
    DeliveryResponse.NodeInfo latestProgressedNode = null;
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

    List<ShipmentStatus> statuses =
        shipments.stream().map(r -> ShipmentStatus.valueOf(r.get(P_SHIPMENT.STATUS))).toList();

    return new DeliveryResponse.ListItem(
        first.get(P_DELIVERY.ID),
        first.get(P_DELIVERY.ORDER_ID),
        deriveDeliveryStatus(statuses),
        fromHub,
        toVendor,
        totalShipments,
        completedShipments,
        latestProgressedNode,
        lastShipmentStatus,
        first.get(P_DELIVERY.CREATED_AT).toLocalDateTime());
  }

  public List<ShipmentResponse.Detail> toShipmentDetailList(Result<Record> records) {
    return records.map(this::toShipmentDetail);
  }

  protected ShipmentResponse.Detail toShipmentDetail(Record record) {
    return new ShipmentResponse.Detail(
        record.get(P_SHIPMENT.ID),
        record.get(P_SHIPMENT.SEQUENCE),
        ShipmentStatus.valueOf(record.get(P_SHIPMENT.STATUS)),
        ShipmentType.valueOf(record.get(P_SHIPMENT.TYPE)),
        fromNodeOf(record),
        toNodeOf(record),
        record.get(P_SHIPMENT.DELIVERY_MANAGER_ID) != null
            ? new DeliveryResponse.ManagerInfo(
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

  private DeliveryStatus deriveDeliveryStatus(List<ShipmentStatus> statuses) {
    if (statuses.isEmpty()) return DeliveryStatus.READY;
    if (statuses.stream().anyMatch(s -> s == ShipmentStatus.FAILED)) return DeliveryStatus.FAILED;
    if (statuses.stream().allMatch(s -> s == ShipmentStatus.COMPLETED))
      return DeliveryStatus.COMPLETED;
    if (statuses.stream().anyMatch(ShipmentStatus::isInProgress)) return DeliveryStatus.IN_PROGRESS;
    if (statuses.stream().allMatch(s -> s == ShipmentStatus.CANCELLED))
      return DeliveryStatus.CANCELLED;
    return DeliveryStatus.READY;
  }

  private DeliveryResponse.NodeInfo fromNodeOf(Record record) {
    return new DeliveryResponse.NodeInfo(
        record.get(P_SHIPMENT.FROM_NODE_ID),
        NodeType.valueOf(record.get(P_SHIPMENT.FROM_NODE_TYPE)),
        record.get(P_SHIPMENT.FROM_NODE_NAME));
  }

  private DeliveryResponse.NodeInfo toNodeOf(Record record) {
    return new DeliveryResponse.NodeInfo(
        record.get(P_SHIPMENT.TO_NODE_ID),
        NodeType.valueOf(record.get(P_SHIPMENT.TO_NODE_TYPE)),
        record.get(P_SHIPMENT.TO_NODE_NAME));
  }
}
