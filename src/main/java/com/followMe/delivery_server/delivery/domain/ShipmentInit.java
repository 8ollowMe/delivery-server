package com.followMe.delivery_server.delivery.domain;

import com.followMe.delivery_server.delivery.domain.enums.ShipmentType;

public record ShipmentInit(ShipmentType shipmentType, int sequence, Node fromNode, Node toNode) {}
