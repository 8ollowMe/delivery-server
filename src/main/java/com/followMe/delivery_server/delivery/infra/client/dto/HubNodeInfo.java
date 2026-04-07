package com.followMe.delivery_server.delivery.infra.client.dto;

import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import java.math.BigDecimal;
import java.util.UUID;

public record HubNodeInfo(
    UUID id,
    NodeType type,
    String name,
    String address,
    int sequence,
    BigDecimal distance,
    BigDecimal duration) {}
