package com.followMe.delivery_server.delivery.infra.query;

import static com.followMe.delivery_server.jooq.tables.PDelivery.P_DELIVERY;
import static com.followMe.delivery_server.jooq.tables.PShipment.P_SHIPMENT;

import com.followMe.delivery_server.delivery.domain.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.enums.DeliverySortBy;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
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
      conditions.add(deliveryStatusCondition(condition.getStatus()));

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

  private Condition deliveryStatusCondition(DeliveryStatus status) {
    return switch (status) {
      case FAILED ->
          DSL.exists(
              DSL.selectOne()
                  .from(P_SHIPMENT)
                  .where(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
                  .and(P_SHIPMENT.STATUS.eq("FAILED")));
      case IN_PROGRESS ->
          DSL.exists(
              DSL.selectOne()
                  .from(P_SHIPMENT)
                  .where(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
                  .and(P_SHIPMENT.STATUS.in("SHIPPED", "IN_TRANSIT", "ARRIVED")));
      case COMPLETED ->
          DSL.notExists(
              DSL.selectOne()
                  .from(P_SHIPMENT)
                  .where(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
                  .and(P_SHIPMENT.STATUS.ne("COMPLETED")));
      case CANCELLED ->
          DSL.notExists(
              DSL.selectOne()
                  .from(P_SHIPMENT)
                  .where(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
                  .and(P_SHIPMENT.STATUS.ne("CANCELLED")));
      case READY ->
          DSL.notExists(
              DSL.selectOne()
                  .from(P_SHIPMENT)
                  .where(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
                  .and(P_SHIPMENT.STATUS.ne("PENDING")));
    };
  }

  public SortField<?> buildSortField(DeliverySearchCondition condition) {
    Field<?> field =
        switch (condition.getSortBy()) {
          case SHIPPED_AT -> P_SHIPMENT.SHIPPED_AT;
          case ARRIVED_AT -> P_SHIPMENT.ARRIVED_AT;
          case STATUS -> derivedStatusOrder();
          default -> P_DELIVERY.CREATED_AT;
        };
    return condition.getDirection() == Sort.Direction.ASC ? field.asc() : field.desc();
  }

  private Field<Integer> derivedStatusOrder() {
    return DSL.when(
            DSL.exists(
                DSL.selectOne()
                    .from(P_SHIPMENT)
                    .where(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
                    .and(P_SHIPMENT.STATUS.eq("FAILED"))),
            4)
        .when(
            DSL.exists(
                DSL.selectOne()
                    .from(P_SHIPMENT)
                    .where(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
                    .and(P_SHIPMENT.STATUS.in("SHIPPED", "IN_TRANSIT", "ARRIVED"))),
            2)
        .when(
            DSL.notExists(
                DSL.selectOne()
                    .from(P_SHIPMENT)
                    .where(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
                    .and(P_SHIPMENT.STATUS.ne("COMPLETED"))),
            3)
        .when(
            DSL.notExists(
                DSL.selectOne()
                    .from(P_SHIPMENT)
                    .where(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
                    .and(P_SHIPMENT.STATUS.ne("CANCELLED"))),
            5)
        .otherwise(1);
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
