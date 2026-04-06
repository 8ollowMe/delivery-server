package com.followMe.delivery_server.delivery.domain;

import java.util.UUID;

public record UserInfo(UUID id, String name, UUID hubId, UUID vendorId, UserRole role) {}
