package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.delivery_server.delivery.domain.RouteNode;
import java.util.List;
import java.util.UUID;

public interface HubRouteInfo {
  List<RouteNode> getRouteNodes(UUID fromHubId, UUID toVendorId);
}
