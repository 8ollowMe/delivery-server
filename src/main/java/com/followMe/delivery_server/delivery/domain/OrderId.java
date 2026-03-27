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
public class OrderId {
  @Column(nullable = false, columnDefinition = "uuid")
  private UUID value;

  public static OrderId of(UUID value) {
    return new OrderId(value);
  }
}
