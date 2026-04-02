package com.followMe.delivery_server.delivery.infra.query;

import static com.followMe.delivery_server.jooq.tables.PDelivery.P_DELIVERY;
import static com.followMe.delivery_server.jooq.tables.PShipment.P_SHIPMENT;

import com.followMe.delivery_server.delivery.domain.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.enums.DeliverySortBy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeliveryConditionBuilder {

  private final DSLContext dsl;

  public SelectOrderByStep<Record1<UUID>> buildDeliveryIdQuery(DeliverySearchCondition condition) {
    List<Condition> conditions = buildConditions(condition);
    if (needsShipmentJoin(condition)) {
      return dsl.select(P_DELIVERY.ID)
          .from(P_DELIVERY)
          .leftJoin(P_SHIPMENT)
          .on(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
          .where(conditions)
          .groupBy(P_DELIVERY.ID);
    }
    return dsl.select(P_DELIVERY.ID).from(P_DELIVERY).where(conditions);
  }

  private List<Condition> buildConditions(DeliverySearchCondition condition) {
    List<Condition> conditions = new ArrayList<>();
    conditions.add(P_DELIVERY.DELETED_AT.isNull());

    if (condition.getStatus() != null)
      conditions.add(P_DELIVERY.STATUS.eq(condition.getStatus().name()));

    if (condition.getOrderId() != null)
      conditions.add(P_DELIVERY.ORDER_ID.eq(condition.getOrderId()));

    if (condition.getDeliveryManagerId() != null)
      conditions.add(P_SHIPMENT.DELIVERY_MANAGER_ID.eq(condition.getDeliveryManagerId()));

    if (condition.getHubId() != null)
      conditions.add(
          P_SHIPMENT
              .FROM_NODE_ID
              .eq(condition.getHubId())
              .or(P_SHIPMENT.TO_NODE_ID.eq(condition.getHubId())));

    if (condition.getVendorId() != null)
      conditions.add(P_SHIPMENT.TO_NODE_ID.eq(condition.getVendorId()));

    if (condition.getKeyword() != null) {
      String like = "%" + condition.getKeyword() + "%";
      conditions.add(
          P_SHIPMENT
              .DELIVERY_MANAGER_NAME
              .likeIgnoreCase(like)
              .or(P_SHIPMENT.FROM_NODE_NAME.likeIgnoreCase(like))
              .or(P_SHIPMENT.TO_NODE_NAME.likeIgnoreCase(like)));
    }
    return conditions;
  }

  public SortField<?> buildSortField(DeliverySearchCondition condition) {
    Field<?> field =
        switch (condition.getSortBy()) {
          case SHIPPED_AT -> P_SHIPMENT.SHIPPED_AT;
          case ARRIVED_AT -> P_SHIPMENT.ARRIVED_AT;
          case STATUS -> P_DELIVERY.STATUS;
          default -> P_DELIVERY.CREATED_AT;
        };
    return condition.getDirection() == Sort.Direction.ASC ? field.asc() : field.desc();
  }

  public boolean needsShipmentJoin(DeliverySearchCondition condition) {
    return condition.getDeliveryManagerId() != null
        || condition.getHubId() != null
        || condition.getVendorId() != null
        || condition.getKeyword() != null
        || condition.getSortBy() == DeliverySortBy.SHIPPED_AT
        || condition.getSortBy() == DeliverySortBy.ARRIVED_AT;
  }
}
