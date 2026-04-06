package com.followMe.delivery_server.delivery.application.dto;

import java.util.UUID;

public class OrderRequest {

  public record DeliveryAssigned(UUID deliveryManagerId) {}
}
