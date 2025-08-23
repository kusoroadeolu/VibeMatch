package com.victor.VibeMatch.rabbitmq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
@ConfigurationProperties("spring.rabbitmq.factory")
public class RabbitFactoryConfigProperties {
    private String cloudHost;
    private int port;
    private String username;
    private String vHost;
    private String password;
}
