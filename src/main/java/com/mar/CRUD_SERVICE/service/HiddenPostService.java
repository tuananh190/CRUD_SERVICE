package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.model.HiddenPost;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.Topic;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.UserInterest;
import com.mar.CRUD_SERVICE.repository.HiddenPostRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserInterestRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class HiddenPostService {

    private final HiddenPostRepository hiddenPostRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserInterestRepository userInterestRepository;

    public HiddenPostService(HiddenPostRepository hiddenPostRepository,
            UserRepository userRepository,
            PostRepository postRepository,
            UserInterestRepository userInterestRepository) {
        this.hiddenPostRepository = hiddenPostRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.userInterestRepository = userInterestRepository;
    }

    @Transactional
    public String hidePost(String username, Long postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        if (post.getAuthor() != null && post.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không thể ẩn bài viết của chính mình.");
        }

        if (hiddenPostRepository.existsByUserAndPost(user, post)) {
            throw new IllegalStateException("Bài viết này đã được ẩn khỏi feed của bạn rồi.");
        }

        hiddenPostRepository.save(new HiddenPost(user, post));

        if (post.getTopics() != null) {
            updateUserInterest(user, post.getTopics(), -5);
        }

        return "Đã ẩn bài viết khỏi feed của bạn.";
    }

    @Transactional
    public String unhidePost(String username, Long postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user: " + username));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        HiddenPost hiddenPost = hiddenPostRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new IllegalStateException("Bài viết này chưa được ẩn."));

        hiddenPostRepository.delete(hiddenPost);

        if (post.getTopics() != null) {
            updateUserInterest(user, post.getTopics(), 5);
        }

        return "Đã hoàn tác ẩn bài viết. Bài viết sẽ xuất hiện lại trong feed của bạn.";
    }

    private void updateUserInterest(User user, java.util.List<Topic> topics, int weight) {
        for (Topic topic : topics) {
            UserInterest interest = userInterestRepository.findByUserAndTopic(user, topic)
                    .orElse(new UserInterest(user, topic, 0));
            interest.setScore(interest.getScore() + weight);
            userInterestRepository.save(interest);
        }
    }

    public Set<Long> getHiddenPostIds(User user) {
        if (user == null) {
            return java.util.Collections.emptySet();
        }
        return hiddenPostRepository.findHiddenPostIdsByUser(user);
    }
}
