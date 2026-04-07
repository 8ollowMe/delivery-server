package com.followMe.delivery_server.delivery.domain;

import com.followMe.delivery_server.delivery.domain.enums.NodeType;
import java.math.BigDecimal;
import java.util.UUID;

public record RouteNode(
    UUID id,
    NodeType type,
    String name,
    String address,
    BigDecimal distance,
    BigDecimal duration) {}
