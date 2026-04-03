package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.delivery_server.delivery.domain.Node;
import java.util.List;
import java.util.UUID;

public interface HubRouteInfo {
  List<Node> getRouteNodes(UUID fromHubId, UUID toVendorId);
}
