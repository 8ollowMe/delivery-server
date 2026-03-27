package com.followMe.delivery_server.delivery.domain;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
public class DeliveryManager {
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(length = 50)
  private String name;

  public static DeliveryManager of(UUID deliveryManagerId, String deliveryManagerName) {
    return new DeliveryManager(deliveryManagerId, deliveryManagerName);
  }
}
