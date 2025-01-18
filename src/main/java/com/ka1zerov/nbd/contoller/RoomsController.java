package com.ka1zerov.nbd.contoller;

import com.google.gson.Gson;
import com.ka1zerov.nbd.model.Message;
import com.ka1zerov.nbd.model.Room;
import com.ka1zerov.nbd.model.User;
import com.ka1zerov.nbd.repository.RoomsRepository;
import com.ka1zerov.nbd.repository.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/rooms")
@Slf4j
public class RoomsController {

    @Autowired
    private RoomsRepository roomsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping(value = "user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Room>> getRooms(@PathVariable int userId) {
        Set<String> roomIds = roomsRepository.getUserRoomIds(userId);
        if (roomIds == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Room> rooms = new ArrayList<>();

        for (String roomId : roomIds) {
            if (roomsRepository.isRoomExists(roomId)) {
                String name = roomsRepository.getRoomNameById(roomId);
                if (name == null) {
                    Room privateRoom = handlePrivateRoomCase(roomId);
                    if (privateRoom == null) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                    }
                    rooms.add(privateRoom);
                } else {
                    rooms.add(new Room(roomId, name));
                }
            }
        }
        return ResponseEntity.ok(rooms);
    }

    private String[] parseUserIds(String roomId) {
        String[] userIds = roomId.split(":");
        if (userIds.length != 2) {
            log.error("User ids not parsed properly");
            throw new RuntimeException("Unable to parse user ids from roomId: " + roomId);
        }
        return userIds;
    }

    private Room handlePrivateRoomCase(String roomId) {
        String[] userIds = parseUserIds(roomId);
        User firstUser = usersRepository.getUserById(Integer.parseInt(userIds[0]));
        User secondUser = usersRepository.getUserById(Integer.parseInt(userIds[1]));
        if (firstUser == null || secondUser == null) {
            log.error("Users were not found by ids: {}", Arrays.toString(userIds));
            return null;
        }
        return new Room(roomId, firstUser.getUsername(), secondUser.getUsername());
    }

    @GetMapping(value = "messages/{roomId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Message>> getMessages(@PathVariable String roomId, @RequestParam int offset, @RequestParam int size) {
        if (!roomsRepository.isRoomExists(roomId)) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        Set<String> values = roomsRepository.getMessages(roomId, offset, size);
        List<Message> messages = new ArrayList<>();
        for (String value : values) {
            Message message = deserialize(value);
            if (message != null) {
                messages.add(message);
            }
        }
        return ResponseEntity.ok(messages);
    }

    private Message deserialize(String value) {
        try {
            return new Gson().fromJson(value, Message.class);
        } catch (Exception e) {
            log.error("Couldn't deserialize json: {}", value, e);
            return null;
        }
    }
}
