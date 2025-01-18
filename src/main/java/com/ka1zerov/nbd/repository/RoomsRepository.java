package com.ka1zerov.nbd.repository;

import com.google.gson.Gson;
import com.ka1zerov.nbd.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@Slf4j
public class RoomsRepository {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String USER_ROOMS_KEY = "user:%d:rooms";
    private static final String ROOM_NAME_KEY = "room:%s:name";
    public static final String ROOM_KEY = "room:%s";

    public Set<String> getUserRoomIds(int userId) {
        String userRoomsKey = String.format(USER_ROOMS_KEY, userId);
        Set<String> roomIds = redisTemplate.opsForSet().members(userRoomsKey);
        log.debug("Received roomIds by userId: {}", userId);
        return roomIds;
    }

    public boolean isRoomExists(String roomId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(String.format(ROOM_KEY, roomId)));
    }

    public String getRoomNameById(String roomId) {
        String roomNameKey = String.format(ROOM_NAME_KEY, roomId);
        return redisTemplate.opsForValue().get(roomNameKey);
    }

    public Set<String> getMessages(String roomId, int offset, int size) {
        String roomNameKey = String.format(ROOM_KEY, roomId);
        Set<String> messages = redisTemplate.opsForZSet().reverseRange(roomNameKey, offset, offset + size);
        log.debug("Received messages by roomId: {}, offset: {}, size: {}", roomId, offset, size);
        return messages;
    }

    public void sendMessageToRedis(String topic, String serializedMessage) {
        log.debug("Saving message to Redis: topic: {}, message: {}", topic, serializedMessage);
        redisTemplate.convertAndSend(topic, serializedMessage);
    }

    public void saveMessage(Message message) {
        Gson gson = new Gson();
        String roomKey = String.format(ROOM_KEY, message.getRoomId());
        redisTemplate.opsForZSet().add(roomKey, gson.toJson(message), message.getDate());
    }
}
