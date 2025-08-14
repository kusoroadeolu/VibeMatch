package com.victor.VibeMatch.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final RabbitSyncConfigProperties rabbitSyncConfigProperties;

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, @Qualifier("rabbitExecutor") ThreadPoolTaskExecutor asyncRabbitListenerExecutor){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setTaskExecutor(asyncRabbitListenerExecutor);
        factory.setPrefetchCount(1);
        return factory;
    }

    @Bean(name = "rabbitExecutor")
    public ThreadPoolTaskExecutor asyncRabbitListenerExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("RabbitListener-");
        executor.initialize();
        return executor;
    }

    @Bean
    public Queue syncQueue(){
        return new Queue(rabbitSyncConfigProperties.getQueueName(), false);
    }

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(rabbitSyncConfigProperties.getExchangeName());
    }

    @Bean
    public Binding binding(Queue syncQueue, DirectExchange directExchange){
        return BindingBuilder.bind(syncQueue).to(directExchange).with(rabbitSyncConfigProperties.getRoutingKey());
    }

    @Bean
    public DirectExchange deadLetterExchange(){
        return new DirectExchange(rabbitSyncConfigProperties.getExchangeName() + ".dlx");
    }

    @Bean
    public Queue delayQueue(){
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", rabbitSyncConfigProperties.getTtl());
        args.put("x-dead-letter-exchange", rabbitSyncConfigProperties.getExchangeName());
        args.put("x-dead-letter-routing-key", rabbitSyncConfigProperties.getRoutingKey());
        return new Queue(rabbitSyncConfigProperties.getQueueName() + ".delay", false, false, true, args);
    }

    @Bean
    public Binding deadLetterBinding(Queue delayQueue, @Qualifier("deadLetterExchange") DirectExchange deadLetterExchange){
        return BindingBuilder.bind(delayQueue).to(deadLetterExchange).with(rabbitSyncConfigProperties.getRoutingKey());
    }


}
