package theBugApp.backend.service;

import theBugApp.backend.entity.Vote;

public interface VoteService {
    int voteQuestion(Long questionId, String userEmail, Vote.VoteType voteType);
    int voteAnswer(Long answerId, String userEmail, Vote.VoteType voteType);
}