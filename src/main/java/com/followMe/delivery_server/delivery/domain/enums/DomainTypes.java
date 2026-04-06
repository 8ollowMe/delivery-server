package com.followMe.delivery_server.delivery.domain.enums;

public enum DomainTypes {
  DELIVERY("DELIVERY"),
  SHIPMENT("SHIPMENT");

  private final String type;

  DomainTypes(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
