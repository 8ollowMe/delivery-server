package com.followMe.delivery_server.delivery.domain.enums;

public enum DeliveryStatus {
  HUB_WAITING,
  HUB_MOVING,
  DESTINATION_HUB_ARRIVED,
  DELIVERING,
  VENDOR_MOVING,
  COMPLETED,
  FAILED,
  CANCELLED
}
