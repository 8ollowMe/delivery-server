package com.followMe.delivery_server.delivery.application.dto;

import java.util.UUID;

public record OrderCreateCommand(UUID orderId, UUID sourceHubId, UUID vendorId) {}
