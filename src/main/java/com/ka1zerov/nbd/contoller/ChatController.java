package com.ka1zerov.nbd.contoller;

import com.google.gson.Gson;
import com.ka1zerov.nbd.model.ChatControllerMessage;
import com.ka1zerov.nbd.model.Message;
import com.ka1zerov.nbd.model.MessageType;
import com.ka1zerov.nbd.model.PubSubMessage;
import com.ka1zerov.nbd.model.User;
import com.ka1zerov.nbd.repository.RoomsRepository;
import com.ka1zerov.nbd.repository.UsersRepository;
import com.ka1zerov.nbd.service.RedisMessageSubscriber;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@RestController
@RequestMapping("/chat")
@Slf4j
@AllArgsConstructor
public class ChatController {
    private UsersRepository usersRepository;
    private RoomsRepository roomsRepository;
    private ChannelTopic topic;
    private MessageListenerAdapter messageListener;

    @RequestMapping("/stream")
    public SseEmitter streamSseMvc(@RequestParam int userId) {
        AtomicBoolean isComplete = new AtomicBoolean(false);
        SseEmitter emitter = new SseEmitter();

        Function<String, Integer> handler = (String message) -> {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                log.warn("Error sending SSE event: ", e);
                return 1;
            }
            return 0;
        };

        RedisMessageSubscriber redisMessageSubscriber = (RedisMessageSubscriber) messageListener.getDelegate();
        redisMessageSubscriber.attach(handler);

        Runnable onDetach = () -> {
            redisMessageSubscriber.detach(handler);
            if (isComplete.compareAndSet(false, true)) {
                emitter.complete();
            }
        };

        emitter.onCompletion(onDetach);
        emitter.onError((err) -> onDetach.run());
        emitter.onTimeout(onDetach);

        return emitter;
    }

    @RequestMapping(value = "/emit", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> emitMessage(@RequestBody ChatControllerMessage chatMessage) {
        log.info("Received message: {}", chatMessage);

        String serializedMessage;
        if (chatMessage.getType() == MessageType.MESSAGE) {
            serializedMessage = handleRegularMessageCase(chatMessage);
        } else if (chatMessage.getType() == MessageType.USER_CONNECTED || chatMessage.getType() == MessageType.USER_DISCONNECTED) {
            serializedMessage = handleUserConnectionCase(chatMessage);
        } else {
            serializedMessage = new Gson().toJson(new PubSubMessage<>(chatMessage.getType().value(), chatMessage.getData()));
        }

        roomsRepository.sendMessageToRedis(topic.getTopic(), serializedMessage);

        return ResponseEntity.ok().build();
    }

    private String handleRegularMessageCase(ChatControllerMessage chatMessage) {
        Gson gson = new Gson();
        Message message = gson.fromJson(chatMessage.getData(), Message.class);
        usersRepository.addUserToOnlineList(message.getFrom());
        roomsRepository.saveMessage(message);
        return gson.toJson(new PubSubMessage<>(chatMessage.getType().value(), message));
    }

    private String handleUserConnectionCase(ChatControllerMessage chatMessage) {
        Gson gson = new Gson();
        int userId = chatMessage.getUser().getId();
        String messageType = chatMessage.getType().value();
        User serializedUser = gson.fromJson(chatMessage.getData(), User.class);
        String serializedMessage = gson.toJson(new PubSubMessage<>(messageType, serializedUser));

        if (chatMessage.getType() == MessageType.USER_CONNECTED) {
            usersRepository.addUserToOnlineList(String.valueOf(userId));
        } else {
            usersRepository.removeUserFromOnlineList(String.valueOf(userId));
        }
        return serializedMessage;
    }
}
