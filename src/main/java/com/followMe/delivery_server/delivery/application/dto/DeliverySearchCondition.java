package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.enums.DeliverySortBy;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class DeliverySearchCondition {

  private DeliveryStatus status;
  private UUID deliveryManagerId;
  private UUID orderId;
  private UUID hubId;
  private UUID vendorId;
  private String keyword;
  private DeliverySortBy sortBy = DeliverySortBy.CREATED_AT;
  private Sort.Direction direction = Sort.Direction.DESC;
}
