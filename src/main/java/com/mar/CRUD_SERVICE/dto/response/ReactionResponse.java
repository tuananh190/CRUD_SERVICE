package com.mar.CRUD_SERVICE.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * DTO trả về kết quả của một hành động Reaction (thả / đổi / huỷ cảm xúc).
 *
 * Thiết kế theo chuẩn "Descriptive Response" — giúp client biết chính xác
 * điều gì đã xảy ra mà không cần gọi thêm API để refresh dữ liệu.
 *
 * Ví dụ response khi thả LIKE thành công:
 * {
 *   "action": "ADDED",
 *   "reactionType": "LIKE",
 *   "totalReactions": 42,
 *   "reactionBreakdown": { "LIKE": 40, "ANGRY": 2 }
 * }
 *
 * Ví dụ response khi hủy LIKE (Toggle off):
 * {
 *   "action": "REMOVED",
 *   "reactionType": "LIKE",
 *   "totalReactions": 41,
 *   "reactionBreakdown": { "LIKE": 39, "ANGRY": 2 }
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Không hiển thị các field null trong JSON output
public class ReactionResponse {

    /**
     * Hành động đã xảy ra:
     * - "ADDED"   : Thả reaction mới
     * - "CHANGED" : Đổi loại reaction (ví dụ: LIKE → ANGRY)
     * - "REMOVED" : Hủy reaction (Toggle off cùng loại)
     */
    private String action;

    /**
     * Loại reaction đã được áp dụng (LIKE, ANGRY...).
     * Null nếu action = "REMOVED" (vì đã xoá, không còn reaction type nào).
     */
    private String reactionType;

    /** Tổng số reaction của bài viết/comment sau khi thực hiện hành động */
    private long totalReactions;

    /**
     * Bảng phân tích chi tiết từng loại reaction.
     * Ví dụ: { "LIKE": 40, "ANGRY": 2 }
     * Chỉ trả về các loại có count > 0 để tránh JSON dư thừa.
     */
    private Map<String, Long> reactionBreakdown;

    public ReactionResponse() {}

    public ReactionResponse(String action, String reactionType, long totalReactions, Map<String, Long> reactionBreakdown) {
        this.action = action;
        this.reactionType = reactionType;
        this.totalReactions = totalReactions;
        this.reactionBreakdown = reactionBreakdown;
    }

    // ==================== Getters & Setters ====================

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getReactionType() { return reactionType; }
    public void setReactionType(String reactionType) { this.reactionType = reactionType; }

    public long getTotalReactions() { return totalReactions; }
    public void setTotalReactions(long totalReactions) { this.totalReactions = totalReactions; }

    public Map<String, Long> getReactionBreakdown() { return reactionBreakdown; }
    public void setReactionBreakdown(Map<String, Long> reactionBreakdown) { this.reactionBreakdown = reactionBreakdown; }
}
