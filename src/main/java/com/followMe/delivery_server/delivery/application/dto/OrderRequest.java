package com.followMe.delivery_server.delivery.presentation.dto;

import java.util.UUID;

public class OrderRequest {

  public record DeliveryAssigned(UUID deliveryManagerId) {}
}
