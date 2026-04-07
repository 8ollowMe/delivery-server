package com.followMe.delivery_server.delivery.infra.client;

import com.followMe.delivery_server.delivery.domain.RouteNode;
import com.followMe.delivery_server.delivery.domain.service.HubRouteInfo;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HubRouteInfoImpl implements HubRouteInfo {
  private final HubClient client;

  @Override
  public List<RouteNode> getRouteNodes(UUID fromHubId, UUID toVendorId) {
    return client.getNodes(fromHubId, toVendorId).nodes().stream()
        .map(
            n -> new RouteNode(n.id(), n.type(), n.name(), n.address(), n.distance(), n.duration()))
        .toList();
  }
}
