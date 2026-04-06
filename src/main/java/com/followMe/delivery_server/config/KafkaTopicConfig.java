package com.followMe.delivery_server.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("!feign-test")
public class KafkaTopicConfig {

  @Bean
  public NewTopic deliveryAssignedTopic() {
    return TopicBuilder.name("DeliveryAssigned").partitions(3).replicas(3).build();
  }

  @Bean
  public NewTopic deliveryCompletedTopic() {
    return TopicBuilder.name("DeliveryCompleted").partitions(3).replicas(3).build();
  }

  @Bean
  public NewTopic shipmentCompletedTopic() {
    return TopicBuilder.name("ShipmentCompleted").partitions(3).replicas(3).build();
  }
}
