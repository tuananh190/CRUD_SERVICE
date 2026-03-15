package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.response.PostResponse;
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

    public List<PostResponse> getRecommendedPosts(String username, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));

        List<UserInterest> interests = userInterestRepository.findByUserOrderByScoreDesc(user);
        if (interests.isEmpty()) {
            // nếu chưa có lịch sử quan tâm, fallback: trả về các bài viết mới nhất
            return postRepository.findAll().stream()
                    .sorted(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(limit)
                    .map(postService::mapToResponse)
                    .collect(Collectors.toList());
        }

        // lấy danh sách topic quan tâm nhiều nhất
        List<Topic> topics = interests.stream()
                .map(UserInterest::getTopic)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (topics.isEmpty()) {
            return postRepository.findAll().stream()
                    .sorted(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(limit)
                    .map(postService::mapToResponse)
                    .collect(Collectors.toList());
        }

        // lấy tất cả post có chứa bất kỳ topic nào trong danh sách quan tâm
        List<Post> candidatePosts = postRepository.findDistinctByTopicsInOrderByCreatedAtDesc(topics);

        // tính điểm cho từng post dựa trên tổng score các topic mà user quan tâm
        Map<Long, Integer> topicScoreMap = new HashMap<>();
        for (UserInterest interest : interests) {
            if (interest.getTopic() != null) {
                topicScoreMap.put(interest.getTopic().getId(), interest.getScore());
            }
        }

        return candidatePosts.stream()
                .sorted((p1, p2) -> {
                    int score1 = computePostScore(p1, topicScoreMap);
                    int score2 = computePostScore(p2, topicScoreMap);
                    if (score1 != score2) {
                        return Integer.compare(score2, score1); // desc theo score
                    }
                    // nếu score bằng nhau thì mới nhất trước
                    return Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                            .compare(p1, p2);
                })
                .limit(limit)
                .map(postService::mapToResponse)
                .collect(Collectors.toList());
    }

    private int computePostScore(Post post, Map<Long, Integer> topicScoreMap) {
        if (post.getTopics() == null) {
            return 0;
        }
        int total = 0;
        for (Topic topic : post.getTopics()) {
            if (topic != null && topic.getId() != null) {
                total += topicScoreMap.getOrDefault(topic.getId(), 0);
            }
        }
        return total;
    }
}

