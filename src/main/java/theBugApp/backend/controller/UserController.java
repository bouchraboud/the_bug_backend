package theBugApp.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.User;
import theBugApp.backend.exception.EmailNonValideException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.exception.UsernameExistsException;
import theBugApp.backend.service.UserService;

import java.util.logging.Logger;

@RestController
@AllArgsConstructor
@CrossOrigin("*")
public class UserController {
    private static final Logger logger = Logger.getLogger(UserController.class.getName());
    private final UserService userService;

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable(value = "id") Long id) {
        try {
            UserDto user = userService.getUserById(id);
            return ResponseEntity.ok().body(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/register/users")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        try {

            logger.info("Received request to register new user: " + user.getInfoUser().getUsername());
            UserDto savedUser = userService.saveUser(user);
            logger.info("Successfully registered user: " + user.getInfoUser().getUsername());
            return ResponseEntity.ok(savedUser);
        } catch (EmailNonValideException | UsernameExistsException e) {
            logger.warning("Failed to register user: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/register/users/confirmation")
    public ResponseEntity<?> confirmEmail(@RequestParam("token") String token) {
        try {
            User user = userService.confirmEmail(token);
            return ResponseEntity.ok("Email confirmé avec succès");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
