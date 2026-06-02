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

    public List<PostListResponse> getRecommendedPosts(String username, int limit) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));

        List<UserInterest> interests = userInterestRepository.findByUserOrderByScoreDesc(user);

        if (interests.isEmpty()) {
            return postRepository.findAll().stream()
                    .sorted(Comparator.comparing(Post::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(limit)
                    .map(postService::mapToListResponse)
                    .collect(Collectors.toList());
        }

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

        Map<Long, Integer> topicScoreMap = new HashMap<>();
        Map<Long, String> topicNameMap = new HashMap<>();
        for (UserInterest interest : interests) {
            if (interest.getTopic() != null) {
                topicScoreMap.put(interest.getTopic().getId(), interest.getScore());
                topicNameMap.put(interest.getTopic().getId(), interest.getTopic().getName());
            }
        }

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

                    List<String> matched = new ArrayList<>();
                    if (post.getTopics() != null) {
                        for (Topic t : post.getTopics()) {
                            if (t != null && topicScoreMap.containsKey(t.getId())) {
                                matched.add(topicNameMap.get(t.getId()));
                            }
                        }
                    }

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

    public void addUserInterestScore(User user, List<Topic> topics, int weight) {
        if (topics == null || topics.isEmpty()) return;
        for (Topic topic : topics) {
            UserInterest interest = userInterestRepository.findByUserAndTopic(user, topic)
                    .orElse(new UserInterest(user, topic, 0));
            interest.setScore(interest.getScore() + weight);
            userInterestRepository.save(interest);
        }
    }

    public List<UserInterest> getUserInterests(User user) {
        return userInterestRepository.findByUserOrderByScoreDesc(user);
    }
}
