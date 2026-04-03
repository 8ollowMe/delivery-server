package com.followMe.delivery_server.delivery.presentation.dto;

import com.followMe.delivery_server.delivery.domain.DeliverySearchCondition;
import com.followMe.delivery_server.delivery.domain.enums.DeliverySortBy;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Getter
@Setter
public class DeliverySearchRequest {

  private DeliveryStatus status;
  private UUID deliveryManagerId;
  private UUID orderId;
  private UUID hubId;
  private UUID vendorId;
  private String keyword;
  private DeliverySortBy sortBy = DeliverySortBy.CREATED_AT;
  private Sort.Direction direction = Sort.Direction.DESC;

  public DeliverySearchCondition toCondition() {
    DeliverySearchCondition condition = new DeliverySearchCondition();
    condition.setStatus(status);
    condition.setDeliveryManagerId(deliveryManagerId);
    condition.setOrderId(orderId);
    condition.setHubId(hubId);
    condition.setVendorId(vendorId);
    condition.setKeyword(keyword);
    condition.setSortBy(sortBy);
    condition.setDirection(direction);
    return condition;
  }
}
