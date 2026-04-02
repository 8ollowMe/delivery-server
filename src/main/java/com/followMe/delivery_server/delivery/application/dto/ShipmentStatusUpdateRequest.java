package com.followMe.delivery_server.delivery.application.dto;

import com.followMe.delivery_server.delivery.domain.enums.ShipmentStatus;

public record ShipmentStatusUpdateRequest(ShipmentStatus status) {}
