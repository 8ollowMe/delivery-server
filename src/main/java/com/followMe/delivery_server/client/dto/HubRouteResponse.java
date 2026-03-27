package com.followMe.delivery_server.client.dto;

import com.followMe.delivery_server.delivery.domain.Node;
import java.util.List;

public record HubRouteResponse(List<HubNodeInfo> nodes) {
  public List<Node> toDomain() {
    return nodes.stream().map(node -> Node.of(node.type(), node.id(), node.name())).toList();
  }
}
