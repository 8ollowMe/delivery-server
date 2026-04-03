package com.followMe.delivery_server.delivery.domain;

import com.followMe.delivery_server.delivery.domain.enums.DeliverySortBy;
import com.followMe.delivery_server.delivery.domain.enums.DeliveryStatus;
import com.followMe.delivery_server.delivery.domain.exception.ForbiddenException;
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

  public void applyPermission(UserContext user) {
    switch (user.role()) {
      case HUB -> this.hubId = user.hubId();
      case DELIVERY -> this.deliveryManagerId = user.userId();
      case MASTER, VENDOR -> {}
      default -> throw new ForbiddenException();
    }
  }
}
