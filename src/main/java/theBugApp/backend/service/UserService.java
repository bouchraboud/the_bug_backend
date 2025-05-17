package theBugApp.backend.service;


import theBugApp.backend.dto.QuestionResponseDTO;
import theBugApp.backend.dto.UserDto;
import theBugApp.backend.entity.User;
import theBugApp.backend.exception.EmailNonValideException;
import theBugApp.backend.exception.UserNotFoundException;
import theBugApp.backend.exception.UsernameExistsException;

import java.util.List;

public interface UserService {
    UserDto saveUser(User user) throws EmailNonValideException, UsernameExistsException;
    User confirmEmail(String token);
    void sendConfirmationEmail(String recipientEmail, String confirmationToken);
    void sendEmail(String to, String subject, String htmlBody);
    UserDto getUserById(Long id) throws UserNotFoundException;
    UserDto getUserByEmail(String email) throws UserNotFoundException;
    List<QuestionResponseDTO> getQuestionsByUserId(Long userId) throws UserNotFoundException;
}