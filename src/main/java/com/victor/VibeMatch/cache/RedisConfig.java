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

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ObjectMapper objectMapper;

    @Value("${spring.cache.time-in-hours}")
    private int cachedValuesDuration;

    @Bean
    public RedisCacheConfiguration tokenCache(){
        Jackson2JsonRedisSerializer<TokenDto> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, TokenDto.class);

        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofHours(cachedValuesDuration))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        serializer
                ));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(){
        return (builder) ->
                builder.withCacheConfiguration("tokenCache", tokenCache());
    }
}
