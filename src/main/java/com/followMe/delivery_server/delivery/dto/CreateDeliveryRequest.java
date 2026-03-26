package com.followMe.delivery_server.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateDeliveryRequest(
    @NotNull(message = "주문 ID는 필수입니다.") UUID orderId,
    @NotBlank(message = "배달 주소는 필수입니다.") String address) {}
