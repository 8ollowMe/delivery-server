package com.followMe.delivery_server.delivery.infra.hub;

import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import com.followMe.delivery_server.delivery.domain.exception.HubClientUnavailableException;
import com.followMe.delivery_server.delivery.infra.hub.dto.HubNodeInfo;
import com.followMe.delivery_server.delivery.infra.hub.dto.HubRouteResponse;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
@Profile("local")
public class HubClientLocalStub implements HubClient {

  private static final UUID MIDDLE_HUB_ID = UUID.fromString("dddddddd-0000-0000-0000-000000000002");
  private static final String UNKNOWN_SUFFIX = "0000-000000000099";

  @Override
  public HubRouteResponse getNodes(UUID sourceHubId, UUID vendorId) {
    log.info("[LocalStub] HubClient.getNodes hubId={} vendorId={}", sourceHubId, vendorId);
    if (sourceHubId.toString().endsWith(UNKNOWN_SUFFIX)
        || vendorId.toString().endsWith(UNKNOWN_SUFFIX)) {
      throw new HubClientUnavailableException();
    }
    return new HubRouteResponse(
        List.of(
            new HubNodeInfo(sourceHubId, NodeType.HUB, "출발 허브", 1),
            new HubNodeInfo(MIDDLE_HUB_ID, NodeType.HUB, "중간 허브", 2),
            new HubNodeInfo(vendorId, NodeType.VENDOR, "도착 업체", 3)));
  }
}
