package com.followMe.delivery_server.delivery.infra.query;

import static com.followMe.delivery_server.jooq.tables.PDelivery.P_DELIVERY;
import static com.followMe.delivery_server.jooq.tables.PShipment.P_SHIPMENT;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.DeliveryQueryPort;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponse;
import com.followMe.delivery_server.delivery.application.dto.ShipmentResponse;
import com.followMe.delivery_server.delivery.domain.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryNotFoundException;
import com.followMe.delivery_server.delivery.domain.exception.ShipmentNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryQueryRepository implements DeliveryQueryPort {

  private final DSLContext dsl;
  private final DeliveryConditionBuilder conditionBuilder;
  private final DeliveryRecordMapper recordMapper;

  @Override
  public DeliveryResponse.Detail findDeliveryById(UUID deliveryId) {
    Result<Record> records =
        dsl.select()
            .from(P_DELIVERY)
            .leftJoin(P_SHIPMENT)
            .on(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
            .where(P_DELIVERY.ID.eq(deliveryId))
            .orderBy(P_SHIPMENT.SEQUENCE.asc())
            .fetch();

    if (records.isEmpty()) throw new DeliveryNotFoundException();
    return recordMapper.toDetail(records.getFirst(), records.stream());
  }

  @Override
  public PageResponse<DeliveryResponse.ListItem> findDeliveries(
      PageRequest pageRequest, DeliverySearchCondition condition) {

    SortField<?> sortField = conditionBuilder.buildSortField(condition);
    SelectOrderByStep<Record1<UUID>> baseQuery = conditionBuilder.buildDeliveryIdQuery(condition);

    long total = dsl.fetchCount(baseQuery);

    List<UUID> deliveryIds =
        baseQuery
            .orderBy(sortField)
            .limit(pageRequest.getSize())
            .offset((long) pageRequest.getPage() * pageRequest.getSize())
            .fetch(P_DELIVERY.ID);

    if (deliveryIds.isEmpty()) {
      return PageResponse.of(List.of(), pageRequest.getPage(), pageRequest.getSize(), total);
    }

    Result<Record> records =
        dsl.select()
            .from(P_DELIVERY)
            .leftJoin(P_SHIPMENT)
            .on(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
            .where(P_DELIVERY.ID.in(deliveryIds))
            .orderBy(sortField, P_SHIPMENT.SEQUENCE.asc())
            .fetch();

    List<DeliveryResponse.ListItem> content =
        records.intoGroups(P_DELIVERY.ID).values().stream()
            .map(group -> recordMapper.toListItem(group.getFirst(), group))
            .toList();

    return PageResponse.of(content, pageRequest.getPage(), pageRequest.getSize(), total);
  }

  @Override
  public DeliveryResponse.Detail findDeliveryByOrderId(UUID orderId) {
    Result<Record> records =
        dsl.select()
            .from(P_DELIVERY)
            .leftJoin(P_SHIPMENT)
            .on(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
            .where(P_DELIVERY.ORDER_ID.eq(orderId))
            .orderBy(P_SHIPMENT.SEQUENCE.asc())
            .fetch();

    if (records.isEmpty()) throw new DeliveryNotFoundException();
    return recordMapper.toDetail(records.getFirst(), records.stream());
  }

  @Override
  public List<ShipmentResponse.Detail> findShipmentsByDeliveryId(UUID deliveryId) {
    Result<Record> records =
        dsl.select()
            .from(P_SHIPMENT)
            .where(P_SHIPMENT.DELIVERY_ID.eq(deliveryId))
            .orderBy(P_SHIPMENT.SEQUENCE.asc())
            .fetch();
    return recordMapper.toShipmentDetailList(records);
  }

  @Override
  public ShipmentResponse.Detail findShipmentById(UUID shipmentId) {
    Record record = dsl.select().from(P_SHIPMENT).where(P_SHIPMENT.ID.eq(shipmentId)).fetchOne();
    if (record == null) throw new ShipmentNotFoundException();
    return recordMapper.toShipmentDetail(record);
  }
}
