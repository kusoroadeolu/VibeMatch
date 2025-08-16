package com.victor.VibeMatch.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victor.VibeMatch.auth.dtos.TokenDto;
import com.victor.VibeMatch.synchandler.Task;
import com.victor.VibeMatch.tasteprofile.TasteProfile;
import com.victor.VibeMatch.tasteprofile.dto.TasteProfileResponseDto;
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

    @Value("${spring.cache.token-time-in-hours}")
    private int tokenCacheDuration;

    @Value("${spring.cache.profile-time-in-hours}")
    private int tasteProfileCacheDuration;

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(){
        Jackson2JsonRedisSerializer<TokenDto> tokenSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, TokenDto.class);
        Jackson2JsonRedisSerializer<Task> taskSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Task.class);
        Jackson2JsonRedisSerializer<TasteProfileResponseDto> tasteProfileSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, TasteProfileResponseDto.class);

        return (builder) -> builder
                .withCacheConfiguration("tokenCache",
                        RedisCacheConfiguration
                                .defaultCacheConfig()
                                .entryTtl(Duration.ofHours(tokenCacheDuration))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(tokenSerializer))
                        )
                .withCacheConfiguration("taskCache",
                        RedisCacheConfiguration
                                .defaultCacheConfig()
                                .entryTtl(Duration.ofHours(tokenCacheDuration))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(taskSerializer))
                        )
                .withCacheConfiguration("tasteProfileCache",
                        RedisCacheConfiguration.
                                defaultCacheConfig()
                                .entryTtl(Duration.ofHours(tasteProfileCacheDuration))
                                .disableCachingNullValues()
                                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(tasteProfileSerializer))
                        );

    }
}
