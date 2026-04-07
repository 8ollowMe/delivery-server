package com.followMe.delivery_server.delivery.domain.service;

import com.followMe.delivery_server.delivery.infra.client.dto.HubNodeInfo;
import java.util.List;
import java.util.UUID;

public interface HubRouteInfo {
  List<HubNodeInfo> getRouteNodes(UUID fromHubId, UUID toVendorId);
}
