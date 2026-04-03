package com.followMe.delivery_server.delivery.domain;

import java.util.UUID;

public record UserContext(UUID userId, UserRole role, UUID hubId, UUID vendorId) {}
