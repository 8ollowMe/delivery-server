package com.followMe.delivery_server.delivery.infra.query;

import static com.followMe.delivery_server.jooq.tables.PDelivery.P_DELIVERY;
import static com.followMe.delivery_server.jooq.tables.PShipment.P_SHIPMENT;

import com.followMe.delivery_server.delivery.application.dto.DeliveryResponse;
import com.followMe.delivery_server.delivery.application.dto.ShipmentResponse;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;
import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.jooq.Record;
import org.jooq.Result;
import org.springframework.stereotype.Component;

@Component
public class DeliveryRecordMapper {

  public DeliveryResponse.Detail toDetail(Record first, Stream<Record> records) {
    List<Record> shipmentRecords = records.filter(r -> r.get(P_SHIPMENT.ID) != null).toList();
    List<ShipmentResponse.Detail> shipments =
        shipmentRecords.stream().map(this::toShipmentDetail).toList();

    return new DeliveryResponse.Detail(
        first.get(P_DELIVERY.ID),
        first.get(P_DELIVERY.ORDER_ID),
        deriveDeliveryStatus(shipmentRecords),
        shipments,
        first.get(P_DELIVERY.CREATED_AT).toLocalDateTime());
  }

  public DeliveryResponse.ListItem toListItem(Record first, Result<Record> group) {
    List<Record> shipments = group.stream().filter(r -> r.get(P_SHIPMENT.ID) != null).toList();

    if (shipments.isEmpty()) {
      return new DeliveryResponse.ListItem(
          first.get(P_DELIVERY.ID),
          first.get(P_DELIVERY.ORDER_ID),
          DeliveryStatus.HUB_WAITING,
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

    return new DeliveryResponse.ListItem(
        first.get(P_DELIVERY.ID),
        first.get(P_DELIVERY.ORDER_ID),
        deriveDeliveryStatus(shipments),
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
        record.get(P_SHIPMENT.ESTIMATED_DISTANCE),
        record.get(P_SHIPMENT.ESTIMATED_DURATION),
        record.get(P_SHIPMENT.ACTUAL_DISTANCE),
        record.get(P_SHIPMENT.ACTUAL_DURATION),
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

  private DeliveryStatus deriveDeliveryStatus(List<Record> shipmentRecords) {
    if (shipmentRecords.isEmpty()) return DeliveryStatus.HUB_WAITING;

    List<ShipmentStatus> statuses =
        shipmentRecords.stream()
            .map(r -> ShipmentStatus.valueOf(r.get(P_SHIPMENT.STATUS)))
            .toList();

    if (statuses.stream().anyMatch(s -> s == ShipmentStatus.FAILED)) return DeliveryStatus.FAILED;
    if (statuses.stream().allMatch(s -> s == ShipmentStatus.CANCELLED))
      return DeliveryStatus.CANCELLED;
    if (statuses.stream().allMatch(s -> s == ShipmentStatus.COMPLETED))
      return DeliveryStatus.COMPLETED;

    // 현재 활성 shipment(가장 작은 sequence 중 완료/취소 아닌 것) 기준으로 세분화
    Record active =
        shipmentRecords.stream()
            .filter(
                r -> {
                  ShipmentStatus s = ShipmentStatus.valueOf(r.get(P_SHIPMENT.STATUS));
                  return s != ShipmentStatus.COMPLETED && s != ShipmentStatus.CANCELLED;
                })
            .min(Comparator.comparingInt(r -> r.get(P_SHIPMENT.SEQUENCE)))
            .orElse(null);

    if (active == null) return DeliveryStatus.COMPLETED;

    ShipmentType type = ShipmentType.valueOf(active.get(P_SHIPMENT.TYPE));
    ShipmentStatus status = ShipmentStatus.valueOf(active.get(P_SHIPMENT.STATUS));

    if (type == ShipmentType.HUB_TO_HUB) {
      return switch (status) {
        case SHIPPED, IN_TRANSIT -> DeliveryStatus.HUB_MOVING;
        case ARRIVED -> DeliveryStatus.DESTINATION_HUB_ARRIVED;
        default -> DeliveryStatus.HUB_WAITING;
      };
    } else {
      return switch (status) {
        case SHIPPED, IN_TRANSIT -> DeliveryStatus.DELIVERING;
        case ARRIVED -> DeliveryStatus.VENDOR_MOVING;
        default -> DeliveryStatus.HUB_WAITING;
      };
    }
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
