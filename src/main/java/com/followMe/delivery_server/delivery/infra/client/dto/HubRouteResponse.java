package com.followMe.delivery_server.delivery.infra.client.dto;

import java.util.List;

public record HubRouteResponse(List<HubNodeInfo> nodes) {

  public List<HubNodeInfo> toNodes() {
    return nodes;
  }
}
