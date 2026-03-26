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
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class DeliveryManager {
  @Column(columnDefinition = "uuid")
  private UUID deliveryManagerId;

  @Column(length = 50)
  private String deliveryManagerName;
}
