package com.ka1zerov.nbd.repository;

import com.ka1zerov.nbd.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class UsersRepository {

    private static final String USERNAME_HASH_KEY = "username";
    private static final String USERNAME_KEY = "username:%s";
    private static final String USER_ID_KEY = "user:%s";
    private static final String ONLINE_USERS_KEY = "online_users";

    @Autowired
    private StringRedisTemplate redisTemplate;

    public User getUserById(int userId) {
        String usernameKey = String.format(USER_ID_KEY, userId);
        String username = (String) redisTemplate.opsForHash().get(usernameKey, USERNAME_HASH_KEY);

        if (username.isEmpty()) {
            log.error("User was not found by id: {}", userId);
            return null;
        }

        boolean isOnline = Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, String.valueOf(userId)));

        return new User(userId, username, isOnline);
    }

    public Set<Integer> getOnlineUsersIds() {
        Set<String> onlineIds = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);

        if (onlineIds.isEmpty()) {
            log.info("No online users found");
            return Set.of();
        }

        return onlineIds.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    public boolean isUserExists(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(String.format(USERNAME_KEY, username)));
    }

    public User getUserByName(String username) {
        if (!isUserExists(username)) {
            return null;
        }

        String userKey = redisTemplate.opsForValue().get(String.format(USERNAME_KEY, username));
        if (userKey.isEmpty()) {
            log.error("Failed to retrieve user key for username: {}", username);
            return null;
        }

        int userId = parseUserId(userKey);
        boolean isOnline = Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, String.valueOf(userId)));

        return new User(userId, username, isOnline);
    }

    private int parseUserId(String userKey) {
        String[] userIds = userKey.split(":");
        return Integer.parseInt(userIds[userIds.length - 1]);
    }

    public void addUserToOnlineList(String userId) {
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
    }

    public void removeUserFromOnlineList(String userId) {
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
    }
}
