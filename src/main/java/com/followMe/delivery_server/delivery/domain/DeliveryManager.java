package com.followMe.delivery_server.delivery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static lombok.AccessLevel.PROTECTED;

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
