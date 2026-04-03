package com.followMe.delivery_server.delivery.infra.client.dto;

import com.followMe.delivery_server.delivery.domain.Node;
import com.followMe.delivery_server.delivery.domain.exception.InvalidNodeInformationException;
import java.util.List;

public record HubRouteResponse(List<HubNodeInfo> nodes) {
  public List<Node> toDomain() {
    if (nodes == null || nodes.size() < 2) throw new InvalidNodeInformationException();
    return nodes.stream().map(node -> Node.of(node.type(), node.id(), node.name())).toList();
  }
}
