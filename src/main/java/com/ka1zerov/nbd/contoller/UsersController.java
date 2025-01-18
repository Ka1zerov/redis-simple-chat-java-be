package com.ka1zerov.nbd.contoller;

import com.google.gson.Gson;
import com.ka1zerov.nbd.config.SessionAttrs;
import com.ka1zerov.nbd.model.User;
import com.ka1zerov.nbd.repository.UsersRepository;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UsersController {
    private UsersRepository usersRepository;

    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, User>> getUsers(@RequestParam(value = "ids") String idsString) {
        Set<Integer> ids = parseIds(idsString);
        Map<String, User> usersMap = new HashMap<>();

        for (Integer id : ids) {
            User user = usersRepository.getUserById(id);
            if (user == null) {
                log.debug("User not found by id: {}", id);
                return ResponseEntity.badRequest().body(Collections.emptyMap());
            }
            usersMap.put(String.valueOf(user.getId()), user);
        }

        return ResponseEntity.ok(usersMap);
    }

    private Set<Integer> parseIds(String idsString) {
        return Arrays.stream(idsString.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    @RequestMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<User> getMe(Model model, HttpSession session) {
        String user = (String) session.getAttribute(SessionAttrs.USER_ATTR_NAME);
        if (user == null) {
            log.debug("User not found in session by attribute: {}", SessionAttrs.USER_ATTR_NAME);
            return ResponseEntity.ok(null);
        }
        return ResponseEntity.ok(new Gson().fromJson(user, User.class));
    }

    @RequestMapping(value = "/online", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, User>> getOnlineUsers() {
        Map<String, User> usersMap = new HashMap<>();
        Set<Integer> onlineIds = usersRepository.getOnlineUsersIds();
        if (onlineIds == null || onlineIds.isEmpty()) {
            log.debug("No online users found!");
            return ResponseEntity.ok(Collections.emptyMap());
        }

        for (Integer onlineId : onlineIds) {
            User user = usersRepository.getUserById(onlineId);
            if (user == null) {
                log.debug("User not found by id: {}", onlineId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
            }
            usersMap.put(String.valueOf(user.getId()), user);
        }

        return ResponseEntity.ok(usersMap);
    }
}


