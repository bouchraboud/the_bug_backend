package theBugApp.backend.enums;

public enum ReputationAction {
    QUESTION_UPVOTE(5, "Question upvoted"),
    QUESTION_DOWNVOTE(-2, "Question downvoted"),
    ANSWER_UPVOTE(10, "Answer upvoted"),
    ANSWER_DOWNVOTE(-2, "Answer downvoted"),
    ANSWER_ACCEPTED(15, "Answer accepted"),
    ANSWER_UNACCEPTED(-15, "Answer unaccepted"),
    QUESTION_ASKED(0, "Question asked"), // No points for asking
    DOWNVOTE_GIVEN(-1, "Gave downvote"), // Cost for downvoting
    DAILY_REPUTATION_CAP(200, "Daily reputation cap"),
    BOUNTY_AWARDED(0, "Bounty awarded"), // Variable amount
    SPAM_PENALTY(-100, "Spam penalty"),
    OFFENSIVE_PENALTY(-100, "Offensive content penalty");

    private final int points;
    private final String description;

    ReputationAction(int points, String description) {
        this.points = points;
        this.description = description;
    }

    public int getPoints() {
        return points;
    }

    public String getDescription() {
        return description;
    }
}
