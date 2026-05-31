package com.mar.CRUD_SERVICE.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReactionResponse {

    private String action;

    private String reactionType;

    private long totalReactions;

    private Map<String, Long> reactionBreakdown;

    public ReactionResponse() {}

    public ReactionResponse(String action, String reactionType, long totalReactions, Map<String, Long> reactionBreakdown) {
        this.action = action;
        this.reactionType = reactionType;
        this.totalReactions = totalReactions;
        this.reactionBreakdown = reactionBreakdown;
    }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public long getTotalReactions() { return totalReactions; }
    public void setTotalReactions(long totalReactions) { this.totalReactions = totalReactions; }

    public Map<String, Long> getReactionBreakdown() { return reactionBreakdown; }
    public void setReactionBreakdown(Map<String, Long> reactionBreakdown) { this.reactionBreakdown = reactionBreakdown; }
}
