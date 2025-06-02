package theBugApp.backend.exception;

public class AnswerNotFoundException extends RuntimeException {
    public AnswerNotFoundException(Long answerId) {
        super("Answer not found with id: " + answerId);
    }
}
