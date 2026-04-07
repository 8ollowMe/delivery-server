package com.followMe.delivery_server.delivery.domain;

import java.util.UUID;

public record UserInfo(UUID userId, String name, UUID hubId, UUID vendorId, UserRole role) {}
