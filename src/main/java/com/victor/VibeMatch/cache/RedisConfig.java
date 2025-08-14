package com.victor.VibeMatch.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victor.VibeMatch.auth.dtos.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;

    @Value("${spring.cache.time-in-hours}")
    private int cachedValuesDuration;

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(){
        Jackson2JsonRedisSerializer<TokenDto> tokenSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, TokenDto.class);
        Jackson2JsonRedisSerializer<Task> taskSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Task.class);

        return (builder) -> builder
                .withCacheConfiguration("tokenCache",
                        RedisCacheConfiguration
                                .defaultCacheConfig()
                                .entryTtl(Duration.ofHours(cachedValuesDuration))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(tokenSerializer))
                        )
                .withCacheConfiguration("taskCache",
                        RedisCacheConfiguration
                                .defaultCacheConfig()
                                .entryTtl(Duration.ofHours(cachedValuesDuration))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(taskSerializer))
                        );

    }
}
