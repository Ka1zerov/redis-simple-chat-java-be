package com.ka1zerov.nbd.contoller;

import com.google.gson.Gson;
import com.ka1zerov.nbd.config.SessionAttrs;
import com.ka1zerov.nbd.model.LoginData;
import com.ka1zerov.nbd.model.User;
import com.ka1zerov.nbd.repository.UsersRepository;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    @Autowired
    private UsersRepository usersRepository;

    /**
     * Create user session by username and password.
     */
    @PostMapping(value = "/login")
    public ResponseEntity<User> login(@RequestBody LoginData loginData, HttpSession session) {
        try {
            String username = loginData.getUsername();

            if (!usersRepository.isUserExists(username)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            User user = usersRepository.getUserByName(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            user.setOnline(true);
            session.setAttribute(SessionAttrs.USER_ATTR_NAME, new Gson().toJson(user));
            log.info("Sign in user: {}", user.getUsername());

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Login error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Dispose the user session.
     */
    @PostMapping(value = "/logout")
    public ResponseEntity<Object> logout(Model model, HttpSession session) {
        Object user = session.getAttribute(SessionAttrs.USER_ATTR_NAME);
        if (user != null) {
            log.info("Sign out user: {}", user);
        }

        session.removeAttribute(SessionAttrs.USER_ATTR_NAME);
        return ResponseEntity.ok().build();
    }
}
