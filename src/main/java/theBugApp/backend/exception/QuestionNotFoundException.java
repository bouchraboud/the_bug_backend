package theBugApp.backend.exception;

public class QuestionNotFoundException extends RuntimeException {
    public QuestionNotFoundException(Long questionId) {
        super("Question not found with id: " + questionId);
    }
}