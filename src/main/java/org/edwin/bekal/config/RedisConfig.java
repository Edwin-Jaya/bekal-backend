package org.edwin.bekal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }

    @Bean
    public RedisSerializer<Object> redisValueSerializer() {
        return RedisSerializer.json();
    }

    @Bean
    public RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(redisValueSerializer()))
                .disableCachingNullValues();
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // ─── Loan ───────────────────────────────────────────────
        cacheConfigs.put("loanDetail",
                defaultCacheConfig().entryTtl(Duration.ofMinutes(15)));
        cacheConfigs.put("customerLoanHistory",
                defaultCacheConfig().entryTtl(Duration.ofMinutes(5)));

        // ─── Master Data ─────────────────────────────────────────
        cacheConfigs.put("branches",
                defaultCacheConfig().entryTtl(Duration.ofHours(1)));

        cacheConfigs.put("roles",
                defaultCacheConfig().entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("rolesPage",
                defaultCacheConfig().entryTtl(Duration.ofHours(1)));

        cacheConfigs.put("menus",
                defaultCacheConfig().entryTtl(Duration.ofHours(1)));

        cacheConfigs.put("bankAccounts",
                defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));

        cacheConfigs.put("internalUsers",
                defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));
        cacheConfigs.put("internalUsersPage",
                defaultCacheConfig().entryTtl(Duration.ofMinutes(30)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig())
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        var keySerializer = new StringRedisSerializer();
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setHashKeySerializer(keySerializer);
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(redisValueSerializer());
        template.setHashValueSerializer(redisValueSerializer());
        template.afterPropertiesSet();
        return template;
    }
}