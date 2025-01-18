package com.ka1zerov.nbd.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

@Service
@Slf4j
public class RedisMessageSubscriber implements MessageListener {
    private final List<Function<String, Integer>> handlers = new CopyOnWriteArrayList<>();

    @Override
    public void onMessage(final Message message, final byte[] pattern) {
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        log.debug("Received message in global subscriber: {}", message);

        handlers.forEach(handler -> handler.apply(messageBody));
    }

    public void attach(Function<String, Integer> handler) {
        if (handler == null) {
            log.warn("Attempted to attach a null handler. Ignored.");
            return;
        }
        handlers.add(handler);
    }

    public void detach(Function<String, Integer> handler) {
        if (handler == null) {
            log.warn("Attempted to detach a null handler. Ignored.");
            return;
        }
        handlers.remove(handler);
    }
}
