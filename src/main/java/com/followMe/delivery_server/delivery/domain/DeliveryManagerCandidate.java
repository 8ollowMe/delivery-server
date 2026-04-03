package com.followMe.delivery_server.delivery.domain;

import java.util.UUID;

public record DeliveryManagerCandidate(UUID id, String name, int sequence, int assignedCount) {}
