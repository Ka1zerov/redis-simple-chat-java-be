package com.ka1zerov.nbd;

import com.google.gson.Gson;
import com.ka1zerov.nbd.model.Message;
import com.ka1zerov.nbd.model.Room;
import com.ka1zerov.nbd.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for creating demo data in the Redis database.
 * This includes users, rooms, and messages to simulate application behavior.
 */
@Service
@Slf4j
public class DemoDataCreator {

    private static final String DEMO_PASSWORD = "password123";
    private static final List<String> DEMO_USERNAME_LIST = Arrays.asList("Pablo", "Joe", "Mary", "Alex");
    private static final List<String> DEMO_GREETING_LIST = Arrays.asList("Hello", "Hi", "Yo", "Hola");
    private static final List<String> DEMO_MESSAGES_LIST = Arrays.asList("Hello", "Hi", "Yo", "Hola");

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public DemoDataCreator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.createDemoData();
    }

    /**
     * Initializes the demo data including users, rooms, and pre-defined messages.
     */
    private void createDemoData() {
        if (Boolean.TRUE.equals(redisTemplate.hasKey("total_users"))) {
            log.info("Demo data already initialized.");
            return;
        }

        log.info("Initializing demo data...");
        redisTemplate.opsForValue().set("total_users", "0");
        redisTemplate.opsForValue().set("room:0:name", "General");

        List<User> users = DEMO_USERNAME_LIST.stream()
                .map(this::createUser)
                .collect(Collectors.toList());

        Map<String, Room> rooms = new HashMap<>();

        users.forEach(user -> {
            users.stream()
                    .filter(otherUser -> otherUser.getId() != user.getId())
                    .forEach(otherUser -> {
                        String privateRoomId = getPrivateRoomId(user.getId(), otherUser.getId());
                        rooms.computeIfAbsent(privateRoomId, id -> createPrivateRoom(user.getId(), otherUser.getId()));
                        addMessage(privateRoomId, String.valueOf(otherUser.getId()), getRandomGreeting(), generateMessageDate());
                    });
        });

        for (int i = 0; i < DEMO_MESSAGES_LIST.size(); i++) {
            int messageDate = getTimestamp() - ((DEMO_MESSAGES_LIST.size() - i) * 200);
            addMessage("0", getRandomUserId(users), DEMO_MESSAGES_LIST.get(i), messageDate);
        }
    }

    /**
     * Creates a user with a given username and stores it in Redis.
     *
     * @param username the username of the user
     * @return the created User object
     */
    private User createUser(String username) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(DEMO_PASSWORD);

        int userId = redisTemplate.opsForValue().increment("total_users").intValue();
        String userKey = String.format("user:%d", userId);
        redisTemplate.opsForValue().set(String.format("username:%s", username), userKey);
        redisTemplate.opsForHash().put(userKey, "username", username);
        redisTemplate.opsForHash().put(userKey, "password", hashedPassword);

        redisTemplate.opsForSet().add(String.format("user:%d:rooms", userId), "0");

        return new User(userId, username, false);
    }

    /**
     * Generates a unique private room ID for two users.
     *
     * @param userId1 the first user ID
     * @param userId2 the second user ID
     * @return the generated room ID
     */
    private String getPrivateRoomId(Integer userId1, Integer userId2) {
        return userId1 < userId2 ? userId1 + ":" + userId2 : userId2 + ":" + userId1;
    }

    /**
     * Creates a private room between two users and stores it in Redis.
     *
     * @param user1 the first user ID
     * @param user2 the second user ID
     * @return the created Room object
     */
    private Room createPrivateRoom(Integer user1, Integer user2) {
        String roomId = getPrivateRoomId(user1, user2);

        redisTemplate.opsForSet().add(String.format("user:%d:rooms", user1), roomId);
        redisTemplate.opsForSet().add(String.format("user:%d:rooms", user2), roomId);

        return new Room(
                roomId,
                (String) redisTemplate.opsForHash().get(String.format("user:%d", user1), "username"),
                (String) redisTemplate.opsForHash().get(String.format("user:%d", user2), "username")
        );
    }

    /**
     * Adds a message to a specified room in Redis.
     *
     * @param roomId   the room ID
     * @param fromId   the sender's user ID
     * @param content  the content of the message
     * @param timeStamp the timestamp of the message
     */
    private void addMessage(String roomId, String fromId, String content, Integer timeStamp) {
        Message message = new Message(fromId, timeStamp, content, roomId);
        redisTemplate.opsForZSet().add(String.format("room:%s", roomId), new Gson().toJson(message), message.getDate());
    }

    /**
     * Generates a random greeting from the predefined list.
     *
     * @return a random greeting string
     */
    private String getRandomGreeting() {
        return DEMO_GREETING_LIST.get(new Random().nextInt(DEMO_GREETING_LIST.size()));
    }

    /**
     * Generates the current timestamp in seconds.
     *
     * @return the current timestamp
     */
    private int getTimestamp() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    /**
     * Generates a random date for a message.
     *
     * @return a random timestamp
     */
    private int generateMessageDate() {
        return getTimestamp() - new Random().nextInt(222);
    }

    /**
     * Selects a random user ID from a list of users.
     *
     * @param users the list of users
     * @return a random user ID as a string
     */
    private String getRandomUserId(List<User> users) {
        return String.valueOf(users.get(new Random().nextInt(users.size())).getId());
    }
}
