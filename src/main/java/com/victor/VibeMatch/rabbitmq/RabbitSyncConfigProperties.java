package com.victor.VibeMatch.rabbitmq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
@ConfigurationProperties("spring.rabbitmq.prop.sync")
public class RabbitSyncConfigProperties {
    private String exchangeName;
    private String queueName;
    private String routingKey;
    private long ttl;
}
