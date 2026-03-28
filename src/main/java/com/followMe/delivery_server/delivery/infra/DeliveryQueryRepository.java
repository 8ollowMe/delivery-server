package com.followMe.delivery_server.delivery.infra;

import static com.followMe.delivery_server.jooq.tables.PDelivery.P_DELIVERY;
import static com.followMe.delivery_server.jooq.tables.PShipment.P_SHIPMENT;

import com.followMe.common.pagination.PageRequest;
import com.followMe.common.pagination.PageResponse;
import com.followMe.delivery_server.delivery.application.dto.DeliveryResponseDto;
import com.followMe.delivery_server.delivery.application.dto.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.exception.DeliveryException.DeliveryNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.Record;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryQueryRepository {

  private final DSLContext dsl;
  private final DeliveryConditionBuilder conditionBuilder;
  private final DeliveryRecordMapper recordMapper;

  public DeliveryResponseDto findDeliveryById(UUID deliveryId) {
    Result<Record> records =
        dsl.select()
            .from(P_DELIVERY)
            .leftJoin(P_SHIPMENT)
            .on(P_SHIPMENT.DELIVERY_ID.eq(P_DELIVERY.ID))
            .where(P_DELIVERY.ID.eq(deliveryId))
            .orderBy(P_SHIPMENT.SEQUENCE.asc())
            .fetch();

    if (records.isEmpty()) throw new DeliveryNotFoundException();
    return recordMapper.toDeliveryResponse(records.getFirst(), records.stream());
  }
}
