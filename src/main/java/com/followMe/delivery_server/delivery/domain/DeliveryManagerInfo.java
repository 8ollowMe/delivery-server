package com.followMe.delivery_server.delivery.domain;

import java.util.UUID;

public record DeliveryManagerInfo(UUID userId, String name, long sequence) {}
