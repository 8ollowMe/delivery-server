package com.followMe.delivery_server.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.Getter;

@Getter
@Embeddable
public class OrderId {
  @Column(nullable = false, columnDefinition = "uuid")
  private UUID value;

  private OrderId(UUID value) {
    this.value = value;
  }

  public OrderId() {}

  public static OrderId of(UUID value) {
    return new OrderId(value);
  }
}
