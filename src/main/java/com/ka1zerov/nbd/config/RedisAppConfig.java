package com.ka1zerov.nbd.config;

import com.ka1zerov.nbd.service.RedisMessageSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Slf4j
@Configuration
public class RedisAppConfig {
    private static final String DEFAULT_REDIS_HOST = "127.0.0.1";
    private static final String DEFAULT_REDIS_PORT = "6379";
    private static final String REDIS_CHANNEL = "MESSAGES";

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "redisTemplate")
    public StringRedisTemplate redisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }

    @Bean
    public MessageListenerAdapter messageListener() {
        return new MessageListenerAdapter(new RedisMessageSubscriber());
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter messageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageListener, topic());
        return container;
    }

    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic(REDIS_CHANNEL);
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = createRedisConfig();
        configurePassword(config);

        log.info("Connecting to Redis at {}:{} with password: {}",
                config.getHostName(),
                config.getPort(),
                config.getPassword().isPresent() ? "provided" : "not provided");

        return new LettuceConnectionFactory(config);
    }

    private RedisStandaloneConfiguration createRedisConfig() {
        String endpoint = System.getenv("REDIS_ENDPOINT_URL");
        String[] hostAndPort = (endpoint != null) ? endpoint.split(":") : new String[]{DEFAULT_REDIS_HOST};

        String host = hostAndPort[0];
        String port = hostAndPort.length > 1 ? hostAndPort[1] : DEFAULT_REDIS_PORT;

        return new RedisStandaloneConfiguration(host, Integer.parseInt(port));
    }

    private void configurePassword(RedisStandaloneConfiguration config) {
        String password = System.getenv("REDIS_PASSWORD");
        if (password != null) {
            config.setPassword(password);
        }
    }
}
