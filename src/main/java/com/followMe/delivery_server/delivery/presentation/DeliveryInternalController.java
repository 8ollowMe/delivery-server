package com.followMe.delivery_server.delivery.presentation;

import com.followMe.delivery_server.delivery.application.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryInternalController {
  private final DeliveryService deliveryService;
}
