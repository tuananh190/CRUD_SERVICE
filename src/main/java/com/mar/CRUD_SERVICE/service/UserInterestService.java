package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.response.PostListResponse;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.Topic;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.UserInterest;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserInterestRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserInterestService {

    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostService postService;

    public UserInterestService(UserInterestRepository userInterestRepository,
                               UserRepository userRepository,
                               PostRepository postRepository,
                               PostService postService) {
        this.userInterestRepository = userInterestRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postService = postService;
    }

    /**
     * Trả về danh sách bài viết gợi ý, kèm theo lý do gợi ý.
     *
     * Mỗi bài viết trong response sẽ có thêm field matchedTopics:
     *   ví dụ: ["travel", "food"]
     *   → "Bài này được gợi ý vì bạn quan tâm đến: travel, food"
     *
     * Nếu user chưa có lịch sử tương tác → fallback về bài mới nhất.
     */
    public List<PostListResponse> getRecommendedPosts(String username, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));

        List<UserInterest> interests = userInterestRepository.findByUserOrderByScoreDesc(user);

        // Fallback: chưa có lịch sử → trả về bài mới nhất (không có matchedTopics)
        if (interests.isEmpty()) {
            return postRepository.findAll().stream()
                    .sorted(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(limit)
                    .map(postService::mapToListResponse)
                    .collect(Collectors.toList());
        }

        // Lấy danh sách topic user quan tâm (theo thứ tự score cao → thấp)
        List<Topic> topics = interests.stream()
                .map(UserInterest::getTopic)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (topics.isEmpty()) {
            return postRepository.findAll().stream()
                    .sorted(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(limit)
                    .map(postService::mapToListResponse)
                    .collect(Collectors.toList());
        }

        // Map topic_id → score để tính nhanh
        Map<Long, Integer> topicScoreMap = new HashMap<>();
        Map<Long, String> topicNameMap = new HashMap<>();
        for (UserInterest interest : interests) {
            if (interest.getTopic() != null) {
                topicScoreMap.put(interest.getTopic().getId(), interest.getScore());
                topicNameMap.put(interest.getTopic().getId(), interest.getTopic().getName());
            }
        }

        // Lấy tất cả bài có chứa ít nhất 1 topic trong danh sách quan tâm
        List<Post> candidatePosts = postRepository.findDistinctByTopicsInOrderByCreatedAtDesc(topics);

        return candidatePosts.stream()
                .sorted((p1, p2) -> {
                    int score1 = computePostScore(p1, topicScoreMap);
                    int score2 = computePostScore(p2, topicScoreMap);
                    if (score1 != score2) return Integer.compare(score2, score1);
                    return Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                            .compare(p1, p2);
                })
                .limit(limit)
                .map(post -> {
                    PostListResponse response = postService.mapToListResponse(post);

                    // Tìm các topic của bài này mà user đang quan tâm → đây là lý do gợi ý
                    List<String> matched = new ArrayList<>();
                    if (post.getTopics() != null) {
                        for (Topic t : post.getTopics()) {
                            if (t != null && topicScoreMap.containsKey(t.getId())) {
                                matched.add(topicNameMap.get(t.getId()));
                            }
                        }
                    }
                    // Gắn matchedTopics vào response — frontend dùng để hiển thị lý do
                    if (!matched.isEmpty()) {
                        response.setMatchedTopics(matched);
                    }
                    return response;
                })
                .collect(Collectors.toList());
    }

    private int computePostScore(Post post, Map<Long, Integer> topicScoreMap) {
        if (post.getTopics() == null) return 0;
        int total = 0;
        for (Topic topic : post.getTopics()) {
            if (topic != null && topic.getId() != null) {
                total += topicScoreMap.getOrDefault(topic.getId(), 0);
            }
        }
        return total;
    }
}
