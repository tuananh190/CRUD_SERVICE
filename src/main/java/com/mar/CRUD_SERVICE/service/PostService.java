package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.PostCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.PostResponse;
import com.mar.CRUD_SERVICE.dto.response.CommentResponse;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.Hashtag;
import com.mar.CRUD_SERVICE.model.Topic;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import com.mar.CRUD_SERVICE.repository.HashtagRepository;
import com.mar.CRUD_SERVICE.repository.TopicRepository;
import com.mar.CRUD_SERVICE.repository.UserInterestRepository;
import com.mar.CRUD_SERVICE.model.UserInterest;
import com.mar.CRUD_SERVICE.service.NotificationService;
import com.mar.CRUD_SERVICE.service.TopicAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    private final TopicRepository topicRepository;
    private final UserInterestRepository userInterestRepository;
    private final NotificationService notificationService;
    private final TopicAnalysisService topicAnalysisService;

    @Autowired
    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       HashtagRepository hashtagRepository,
                       TopicRepository topicRepository,
                       UserInterestRepository userInterestRepository,
                       NotificationService notificationService,
                       TopicAnalysisService topicAnalysisService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.hashtagRepository = hashtagRepository;
        this.topicRepository = topicRepository;
        this.userInterestRepository = userInterestRepository;
        this.notificationService = notificationService;
        this.topicAnalysisService = topicAnalysisService;
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public Post save(Post post) {
        return postRepository.save(post);
    }

    public void deleteById(Long id) {
        postRepository.deleteById(id);
    }

    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PostResponse getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    public PostResponse createPost(PostCreationRequest request) {
        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        // Lấy username từ SecurityContext (yêu cầu đã được xác thực)
        String username = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            username = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        User author = null;
        if (username != null) {
            author = userRepository.findByUsername(username).orElse(null);
        } else if (request.getUserId() != null) {
            // fallback: nếu không có authentication, cho phép chỉ định userId trong request
            author = userRepository.findById(request.getUserId()).orElse(null);
        }

        if (author == null) {
            throw new IllegalStateException("Author (user) not found. Ensure you are authenticated or provide a valid userId.");
        }

        post.setAuthor(author);

        // map location
        post.setLocation(request.getLocation());

        // map shared post (share/repost)
        if (request.getSharedPostId() != null) {
            postRepository.findById(request.getSharedPostId())
                    .ifPresent(post::setSharedPost);
        }

        // map tagged users
        if (request.getTaggedUserIds() != null && !request.getTaggedUserIds().isEmpty()) {
            var taggedUsers = userRepository.findAllById(request.getTaggedUserIds());
            post.setTaggedUsers(taggedUsers);
        }

        // map hashtags
        java.util.List<String> normalizedHashtagNames = java.util.Collections.emptyList();
        if (request.getHashtags() != null && !request.getHashtags().isEmpty()) {
            normalizedHashtagNames = request.getHashtags().stream()
                    .filter(h -> h != null && !h.isBlank())
                    .map(h -> h.startsWith("#") ? h.substring(1) : h)
                    .distinct()
                    .toList();
            var hashtags = normalizedHashtagNames.stream()
                    .map(name -> hashtagRepository.findByName(name)
                            .orElseGet(() -> hashtagRepository.save(new Hashtag(name))))
                    .toList();
            post.setHashtags(hashtags);
        }

        // Phân tích nội dung bằng OpenAI để sinh topics (ngay cả khi không có hashtag)
        var aiTopics = topicAnalysisService.extractTopicsFromContent(post.getContent());
        java.util.Set<String> allTopicNames = new java.util.HashSet<>();
        if (aiTopics != null) {
            aiTopics.stream()
                    .filter(t -> t != null && !t.isBlank())
                    .forEach(t -> allTopicNames.add(t.trim()));
        }
        if (normalizedHashtagNames != null) {
            allTopicNames.addAll(normalizedHashtagNames);
        }

        if (!allTopicNames.isEmpty()) {
            var topicEntities = allTopicNames.stream()
                    .map(name -> topicRepository.findByName(name)
                            .orElseGet(() -> topicRepository.save(new Topic(name))))
                    .toList();
            post.setTopics(topicEntities);
        }

        Post saved = postRepository.save(post);

        // gửi thông báo cho các user được tag trong bài viết
        if (saved.getTaggedUsers() != null) {
            for (User tagged : saved.getTaggedUsers()) {
                if (!tagged.getId().equals(author.getId())) {
                    notificationService.createNotification(
                            tagged,
                            "Bạn được nhắc đến trong một bài viết của @" + author.getUsername()
                    );
                }
            }
        }

        // Cập nhật UserInterest cho tác giả khi tạo bài có sử dụng hashtag (Hashtag = +4)
        if (saved.getTopics() != null && normalizedHashtagNames != null && !normalizedHashtagNames.isEmpty()) {
            java.util.Set<String> hashtagSet = new java.util.HashSet<>(normalizedHashtagNames);
            for (Topic topic : saved.getTopics()) {
                if (topic.getName() != null && hashtagSet.contains(topic.getName())) {
                    UserInterest interest = userInterestRepository.findByUserAndTopic(author, topic)
                            .orElse(new UserInterest(author, topic, 0));
                    interest.setScore(interest.getScore() + 4); // Hashtag -> +4
                    userInterestRepository.save(interest);
                }
            }
        }

        // Cập nhật UserInterest khi chia sẻ/repost bài viết (Share -> +5)
        if (saved.getSharedPost() != null && saved.getSharedPost().getTopics() != null && !saved.getSharedPost().getTopics().isEmpty()) {
            for (Topic topic : saved.getSharedPost().getTopics()) {
                UserInterest interest = userInterestRepository.findByUserAndTopic(author, topic)
                        .orElse(new UserInterest(author, topic, 0));
                interest.setScore(interest.getScore() + 5); // Share -> +5
                userInterestRepository.save(interest);
            }
        }

        return mapToResponse(saved);
    }

    public PostResponse updatePost(Long id, PostCreationRequest request) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());

            Post updated = postRepository.save(post);
            return mapToResponse(updated);
        }
        return null;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public PostResponse mapToResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        response.setLocation(post.getLocation());

        // author
        if (post.getAuthor() != null) {
            PostResponse.UserInfo userInfo = new PostResponse.UserInfo();
            userInfo.setId(post.getAuthor().getId());
            userInfo.setUsername(post.getAuthor().getUsername());
            userInfo.setFirstName(post.getAuthor().getFirstName());
            userInfo.setLastName(post.getAuthor().getLastName());
            response.setAuthor(userInfo);
        }

        // shared post (thông tin bài gốc khi share/repost)
        if (post.getSharedPost() != null) {
            PostResponse shared = new PostResponse();
            shared.setId(post.getSharedPost().getId());
            shared.setTitle(post.getSharedPost().getTitle());
            shared.setContent(post.getSharedPost().getContent());
            shared.setCreatedAt(post.getSharedPost().getCreatedAt());
            if (post.getSharedPost().getAuthor() != null) {
                PostResponse.UserInfo originalAuthor = new PostResponse.UserInfo();
                originalAuthor.setId(post.getSharedPost().getAuthor().getId());
                originalAuthor.setUsername(post.getSharedPost().getAuthor().getUsername());
                originalAuthor.setFirstName(post.getSharedPost().getAuthor().getFirstName());
                originalAuthor.setLastName(post.getSharedPost().getAuthor().getLastName());
                shared.setAuthor(originalAuthor);
            }
            response.setSharedPost(shared);
        }

        // tagged users
        if (post.getTaggedUsers() != null) {
            List<PostResponse.UserInfo> tagged = post.getTaggedUsers().stream().map(u -> {
                PostResponse.UserInfo ui = new PostResponse.UserInfo();
                ui.setId(u.getId());
                ui.setUsername(u.getUsername());
                ui.setFirstName(u.getFirstName());
                ui.setLastName(u.getLastName());
                return ui;
            }).collect(Collectors.toList());
            response.setTaggedUsers(tagged);
        }

        // hashtags -> trả về dưới dạng chuỗi tên
        if (post.getHashtags() != null) {
            List<String> tags = post.getHashtags().stream()
                    .map(h -> h.getName())
                    .collect(Collectors.toList());
            // tái sử dụng trường topics như danh sách tên chủ đề/hashtag
            response.setTopics(tags);
        }

        // comments
        if (post.getComments() != null) {
            List<CommentResponse> comments = post.getComments().stream().map(comment -> {
                CommentResponse cr = new CommentResponse();
                cr.setId(comment.getId());
                cr.setText(comment.getContent());
                cr.setPostId(comment.getPost() != null ? comment.getPost().getId() : null);
                if (comment.getAuthor() != null) {
                    PostResponse.UserInfo a = new PostResponse.UserInfo();
                    a.setId(comment.getAuthor().getId());
                    a.setUsername(comment.getAuthor().getUsername());
                    a.setFirstName(comment.getAuthor().getFirstName());
                    a.setLastName(comment.getAuthor().getLastName());
                    cr.setAuthor(a);
                }
                cr.setCreatedAt(comment.getCreatedAt());

                // tagged users trong comment
                if (comment.getTaggedUsers() != null) {
                    List<PostResponse.UserInfo> taggedInComment = comment.getTaggedUsers().stream().map(u -> {
                        PostResponse.UserInfo ui = new PostResponse.UserInfo();
                        ui.setId(u.getId());
                        ui.setUsername(u.getUsername());
                        ui.setFirstName(u.getFirstName());
                        ui.setLastName(u.getLastName());
                        return ui;
                    }).collect(Collectors.toList());
                    cr.setTaggedUsers(taggedInComment);
                }

                return cr;
            }).collect(Collectors.toList());
            response.setComments(comments);
        }
        return response;
    }
}