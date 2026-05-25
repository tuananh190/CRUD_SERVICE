package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.PostCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.PostResponse;
import com.mar.CRUD_SERVICE.dto.response.CommentResponse;
import com.mar.CRUD_SERVICE.dto.response.PostListResponse;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.model.Hashtag;
import com.mar.CRUD_SERVICE.model.Topic;
import com.mar.CRUD_SERVICE.model.ReactionType;
import com.mar.CRUD_SERVICE.model.Reaction;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
import com.mar.CRUD_SERVICE.repository.HashtagRepository;
import com.mar.CRUD_SERVICE.repository.TopicRepository;
import com.mar.CRUD_SERVICE.repository.UserInterestRepository;
import com.mar.CRUD_SERVICE.repository.ReactionRepository;
import com.mar.CRUD_SERVICE.model.UserInterest;
import com.mar.CRUD_SERVICE.service.NotificationService;
import com.mar.CRUD_SERVICE.service.TopicAnalysisService;
import com.mar.CRUD_SERVICE.model.NotificationType;
import com.mar.CRUD_SERVICE.model.Visibility;
import com.mar.CRUD_SERVICE.repository.FriendshipRepository;
import com.mar.CRUD_SERVICE.service.UserBlockService;
import com.mar.CRUD_SERVICE.service.HiddenPostService;
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
    private final ReactionRepository reactionRepository;
    private final FriendshipRepository friendshipRepository;
    // Inject UserBlockService để filter bài viết của người bị block (BR-Block-01)
    private final UserBlockService userBlockService;
    // Inject HiddenPostService để filter bài viết mà user đã ẩn khỏi feed
    private final HiddenPostService hiddenPostService;

    @Autowired
    public PostService(PostRepository postRepository,
                       UserRepository userRepository,
                       HashtagRepository hashtagRepository,
                       TopicRepository topicRepository,
                       UserInterestRepository userInterestRepository,
                       NotificationService notificationService,
                       TopicAnalysisService topicAnalysisService,
                       ReactionRepository reactionRepository,
                       FriendshipRepository friendshipRepository,
                       UserBlockService userBlockService,
                       HiddenPostService hiddenPostService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.hashtagRepository = hashtagRepository;
        this.topicRepository = topicRepository;
        this.userInterestRepository = userInterestRepository;
        this.notificationService = notificationService;
        this.topicAnalysisService = topicAnalysisService;
        this.reactionRepository = reactionRepository;
        this.friendshipRepository = friendshipRepository;
        this.userBlockService = userBlockService;
        this.hiddenPostService = hiddenPostService;
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

    public List<Post> searchPosts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
    }

    public List<PostListResponse> getAllPosts(String username) {
        // Xác định viewer hiện tại (có thể null nếu chưa đăng nhập)
        User viewer = (username != null)
                ? userRepository.findByUsername(username).orElse(null)
                : null;

        List<Post> posts = postRepository.findAll();

        // [BR-Hide] Lấy tập hợp ID bài viết mà viewer đã ẩn — gọi 1 lần duy nhất
        // Dùng Set<Long> để contains() O(1) khi duyệt qua danh sách bài viết
        // Nếu viewer = null (guest) → hiddenPostIds = empty Set → không filter gì
        java.util.Set<Long> hiddenPostIds = hiddenPostService.getHiddenPostIds(viewer);

        // Lấy bản đồ điểm sở thích của người dùng hiện tại (topic_id → score)
        java.util.Map<Long, Integer> topicScoreMap = new java.util.HashMap<>();
        if (viewer != null) {
            List<UserInterest> interests = userInterestRepository.findByUserOrderByScoreDesc(viewer);
            for (UserInterest interest : interests) {
                topicScoreMap.put(interest.getTopic().getId(), interest.getScore());
            }
        }

        // Sắp xếp: bài có điểm phù hợp cao → lên đầu; nếu bằng điểm → bài mới hơn lên đầu
        posts.sort((a, b) -> {
            int scoreA = calculateRelevanceScore(a, topicScoreMap);
            int scoreB = calculateRelevanceScore(b, topicScoreMap);
            if (scoreB != scoreA) return scoreB - scoreA;
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        // Lọc bài theo quyền hiển thị VÀ bài đã ẩn trước khi trả về
        List<PostListResponse> feed = new java.util.ArrayList<>();
        for (Post post : posts) {
            // [BR-Hide] Bỏ qua bài viết user đã ẩn — O(1) lookup trên HashSet
            if (hiddenPostIds.contains(post.getId())) continue;
            if (canUserViewPost(viewer, post)) {
                feed.add(mapToListResponse(post));
            }
        }
        return feed;
    }

    // Tính tổng điểm phù hợp của một bài viết dựa trên sở thích người dùng
    private int calculateRelevanceScore(com.mar.CRUD_SERVICE.model.Post post, java.util.Map<Long, Integer> topicScoreMap) {
        if (post.getTopics() == null || post.getTopics().isEmpty() || topicScoreMap.isEmpty()) {
            return 0;
        }
        return post.getTopics().stream()
                .mapToInt(topic -> topicScoreMap.getOrDefault(topic.getId(), 0))
                .sum();
    }

    public PostResponse getPostById(Long id, String currentUsername) {
        Optional<Post> optPost = postRepository.findById(id);
        if (optPost.isEmpty()) return null;

        Post post = optPost.get();
        // Xác định viewer và kiểm tra quyền xem
        User viewer = (currentUsername != null)
                ? userRepository.findByUsername(currentUsername).orElse(null)
                : null;

        if (!canUserViewPost(viewer, post)) {
            throw new IllegalStateException("Bài viết này chỉ dành cho bạn bè hoặc trang cá nhân đã bị khóa.");
        }

        return mapToDetailResponse(post, currentUsername);
    }
    
    // Backwards compatibility or internal usage
    public PostResponse getPostById(Long id) {
        return getPostById(id, null);
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
        post.setCreatedAt(java.time.LocalDateTime.now());

        // Đặt quyền hiển thị: nếu không chọn → mặc định PUBLIC
        Visibility visibility = (request.getVisibility() != null)
                ? request.getVisibility()
                : Visibility.PUBLIC;
        post.setVisibility(visibility);

        // map location and geocoding
        if (request.getLocationName() != null && !request.getLocationName().isBlank()) {
            try {
                String loc = java.net.URLEncoder.encode(request.getLocationName(), java.nio.charset.StandardCharsets.UTF_8);
                String url = "https://nominatim.openstreetmap.org/search?q=" + loc + "&format=json&limit=1";
                org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                org.springframework.http.ResponseEntity<java.util.List> response = restTemplate.getForEntity(url, java.util.List.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null && !response.getBody().isEmpty()) {
                    java.util.Map<String, Object> firstResult = (java.util.Map<String, Object>) response.getBody().get(0);
                    post.setLocationName((String) firstResult.get("display_name"));
                    post.setLatitude(Double.parseDouble((String) firstResult.get("lat")));
                    post.setLongitude(Double.parseDouble((String) firstResult.get("lon")));
                } else {
                    post.setLocationName(request.getLocationName());
                }
            } catch (Exception e) {
                // Ignore API failures and just save raw location
                post.setLocationName(request.getLocationName());
            }
        }


        // map tagged users from content (dùng helper method)
        java.util.List<String> extractedMentions = parseMentionedUsernames(request.getContent());
        if (request.getContent() != null) {
            post.setContent(removeMentionSymbols(request.getContent()));
        }
        if (!extractedMentions.isEmpty()) {
            post.setTaggedUsers(userRepository.findAllByUsernameIn(extractedMentions));
        }

        // map hashtags from content with strict validation
        java.util.List<String> normalizedHashtagNames = new java.util.ArrayList<>();
        if (request.getContent() != null) {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?:^|\\s)#([^\\s]+)").matcher(request.getContent());
            while (m.find()) {
                String rawTag = m.group(1).replaceAll("[.,;!?]+$", ""); // Loại bỏ dấu câu ở cuối
                if (!rawTag.matches("^[a-zA-Z]{2,15}$")) {
                    throw new IllegalStateException("Hashtag không hợp lệ. Vui lòng chỉ sử dụng chữ cái (a–z), không chứa ký tự đặc biệt hoặc số, và có độ dài từ 2 đến 15 ký tự.");
                }
                normalizedHashtagNames.add(rawTag.toLowerCase());
            }
        }
        if (!normalizedHashtagNames.isEmpty()) {
            var distinctHashtags = normalizedHashtagNames.stream().distinct().toList();
            
            // Giới hạn số lượng hashtag tối đa là 2
            if (distinctHashtags.size() > 2) {
                throw new IllegalStateException("Hệ thống chỉ cho phép một bài viết chứa tối đa 2 hashtag để tránh tình trạng spam thẻ sai quy định.");
            }

            var hashtags = distinctHashtags.stream()
                    .map(name -> hashtagRepository.findByName(name)
                            .orElseGet(() -> hashtagRepository.save(new Hashtag(name))))
                    .toList();
            post.setHashtags(hashtags);
        }

        // Phân tích nội dung bằng OpenAI để sinh topics (CHỈ khi không có hashtag)
        java.util.Set<String> allTopicNames = new java.util.HashSet<>();
        if (normalizedHashtagNames.isEmpty()) {
            var aiTopics = topicAnalysisService.extractTopicsFromContent(post.getContent());
            if (aiTopics != null) {
                aiTopics.stream()
                        .filter(t -> t != null && !t.isBlank())
                        .forEach(t -> allTopicNames.add(t.trim().toLowerCase()));
            }
        } else {
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
                            author,
                            NotificationType.TAG,
                            post.getId()
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



        return mapToResponse(saved);
    }

    public PostResponse sharePost(Long postId, com.mar.CRUD_SERVICE.dto.request.ShareRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Hết phiên đăng nhập."));
        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Bài viết gốc không tồn tại."));

        // Chặn tự share bài của chính mình
        if (originalPost.getAuthor() != null && originalPost.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("Bạn không thể chia sẻ bài viết của chính mình.");
        }

        // Kiểm tra quyền xem bài gốc trước khi share
        if (!canUserViewPost(user, originalPost)) {
            throw new IllegalStateException("Bạn không có quyền chia sẻ bài viết này.");
        }

        Post sharedPost = new Post();
        sharedPost.setAuthor(user);
        sharedPost.setOriginalPost(originalPost);
        sharedPost.setTitle("");
        sharedPost.setContent(request != null && request.getContent() != null ? request.getContent() : "");
        sharedPost.setCreatedAt(java.time.LocalDateTime.now());

        postRepository.save(sharedPost);

        // Gửi thông báo SHARE cho tác giả bài gốc
        // (tác giả bài gốc biết ai đã chia sẻ bài của mình)
        if (originalPost.getAuthor() != null) {
            notificationService.createNotification(
                    originalPost.getAuthor(),
                    user,
                    com.mar.CRUD_SERVICE.model.NotificationType.SHARE,
                    sharedPost.getId()
            );
        }

        // Cập nhật UserInterest khi chia sẻ bài viết (Share -> +5)
        addUserInterestScore(user, originalPost.getTopics(), 5);

        return mapToDetailResponse(sharedPost, username);
    }

    public PostResponse updatePost(Long id, PostCreationRequest request) {
        // 1. Lấy thông tin người đang thao tác
        String currentUsername = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        if (currentUsername == null) {
            throw new IllegalStateException("Hết phiên đăng nhập hoặc chưa đăng nhập hợp lệ.");
        }

        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();

            // 2. CHỐT CHẶN: Chỉ chủ nhân tạo ra bài viết ban đầu (Kể cả có là Admin) mới được sửa
            if (!post.getAuthor().getUsername().equals(currentUsername)) {
                throw new IllegalStateException("Nạn nhân ngưng ảo tưởng: Bạn không có quyền sửa bài viết của người khác!");
            }

            post.setTitle(request.getTitle());
            post.setContent(request.getContent());

            Post updated = postRepository.save(post);
            return mapToResponse(updated);
        }
        return null;
    }

    public void deletePost(Long id) {
        // Xác định người dùng đang đăng nhập
        String currentUsername = null;
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        if (currentUsername == null) {
            throw new IllegalStateException("Hết phiên đăng nhập hoặc chưa đăng nhập hợp lệ.");
        }

        // Tìm bài viết cần xóa
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Bài viết không tồn tại với id=" + id));

        // Chỉ chủ nhân mới được xóa bài viết của mình
        if (!post.getAuthor().getUsername().equals(currentUsername)) {
            throw new IllegalStateException("Bạn không có quyền xóa bài viết của người khác!");
        }

        postRepository.deleteById(id);
    }

    public PostListResponse mapToListResponse(Post post) {
        long reactionCount = reactionRepository.countByPostId(post.getId());
        int commentCount = post.getComments() != null ? post.getComments().size() : 0;
        long shareCount = postRepository.countByOriginalPostId(post.getId());

        // Công thức tính độ phổ biến: like×1 + comment×2 + share×3
        long popularityScore = (reactionCount * 1L) + (commentCount * 2L) + (shareCount * 3L);

        PostListResponse response = new PostListResponse(
                post.getId(),
                post.getContent(),
                post.getAuthor() != null ? post.getAuthor().getId() : null,
                post.getCreatedAt(),
                reactionCount,
                commentCount
        );
        response.setShareCount(shareCount);
        response.setPopularityScore(popularityScore);

        if (post.getOriginalPost() != null) {
            long origReactionCount = reactionRepository.countByPostId(post.getOriginalPost().getId());
            int origCommentCount = post.getOriginalPost().getComments() != null ? post.getOriginalPost().getComments().size() : 0;
            long origShareCount = postRepository.countByOriginalPostId(post.getOriginalPost().getId());
            long origPopularityScore = (origReactionCount * 1L) + (origCommentCount * 2L) + (origShareCount * 3L);

            PostListResponse origResp = new PostListResponse(
                    post.getOriginalPost().getId(),
                    post.getOriginalPost().getContent(),
                    post.getOriginalPost().getAuthor() != null ? post.getOriginalPost().getAuthor().getId() : null,
                    post.getOriginalPost().getCreatedAt(),
                    origReactionCount,
                    origCommentCount
            );
            origResp.setShareCount(origShareCount);
            origResp.setPopularityScore(origPopularityScore);
            response.setOriginalPost(origResp);
        }
        return response;
    }

    /**
     * Trả về top N bài viết phổ biến nhất, sắp xếp theo popularityScore giảm dần.
     * Công thức: (like × 1) + (comment × 2) + (share × 3)
     * Không cần thêm cột database — tính toán động khi query.
     */
    public List<PostListResponse> getTrendingPosts(int limit) {
        // Lấy viewer hiện tại từ SecurityContext (có thể null nếu guest)
        String currentUsername = null;
        if (org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication() != null) {
            currentUsername = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getName();
        }
        User viewer = (currentUsername != null && !currentUsername.equals("anonymousUser"))
                ? userRepository.findByUsername(currentUsername).orElse(null)
                : null;

        // [BR-Hide] Lấy danh sách bài đã ẩn của viewer — gọi 1 lần trước stream
        // Tránh gọi lặp lại trong vòng lặp stream (N+1 query problem)
        java.util.Set<Long> hiddenPostIds = hiddenPostService.getHiddenPostIds(viewer);

        List<Post> allPosts = postRepository.findAll();
        // Trending chỉ hiển thị bài PUBLIC — bài FRIENDS_ONLY không được tính vào trending
        return allPosts.stream()
                .filter(p -> p.getVisibility() == Visibility.PUBLIC && !p.getAuthor().isPrivate())
                // [BR-Hide] Filter bài đã ẩn — O(1) lookup trên HashSet
                .filter(p -> !hiddenPostIds.contains(p.getId()))
                .map(this::mapToListResponse)
                .sorted((a, b) -> Long.compare(b.getPopularityScore(), a.getPopularityScore()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public PostResponse mapToResponse(Post post) {
        return mapToDetailResponse(post, null);
    }

    public PostResponse mapToDetailResponse(Post post, String currentUsername) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        response.setLocationName(post.getLocationName());
        response.setLatitude(post.getLatitude());
        response.setLongitude(post.getLongitude());

        if (post.getOriginalPost() != null) {
            PostResponse origObj = new PostResponse();
            origObj.setId(post.getOriginalPost().getId());
            origObj.setTitle(post.getOriginalPost().getTitle());
            origObj.setContent(post.getOriginalPost().getContent());
            origObj.setCreatedAt(post.getOriginalPost().getCreatedAt());
            if (post.getOriginalPost().getAuthor() != null) {
                PostResponse.UserInfo origUserInfo = new PostResponse.UserInfo();
                origUserInfo.setId(post.getOriginalPost().getAuthor().getId());
                origUserInfo.setUsername(post.getOriginalPost().getAuthor().getUsername());
                origUserInfo.setFirstName(post.getOriginalPost().getAuthor().getFirstName());
                origUserInfo.setLastName(post.getOriginalPost().getAuthor().getLastName());
                origObj.setAuthor(origUserInfo);
            }
            java.util.Map<String, Long> origReactCounts = new java.util.HashMap<>();
            for (com.mar.CRUD_SERVICE.model.ReactionType type : com.mar.CRUD_SERVICE.model.ReactionType.values()) {
                long count = reactionRepository.countByPostIdAndType(post.getOriginalPost().getId(), type);
                if (count > 0) {
                    origReactCounts.put(type.name(), count);
                }
            }
            origObj.setReactionCounts(origReactCounts);
            response.setOriginalPost(origObj);
        }

        // author
        if (post.getAuthor() != null) {
            PostResponse.UserInfo userInfo = new PostResponse.UserInfo();
            userInfo.setId(post.getAuthor().getId());
            userInfo.setUsername(post.getAuthor().getUsername());
            userInfo.setFirstName(post.getAuthor().getFirstName());
            userInfo.setLastName(post.getAuthor().getLastName());
            response.setAuthor(userInfo);
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

        // Gộp chung hashtags và AI topics để trả về giao diện
        java.util.Set<String> allTags = new java.util.LinkedHashSet<>();
        if (post.getHashtags() != null) {
            post.getHashtags().forEach(h -> allTags.add(h.getName()));
        }
        if (post.getTopics() != null) {
            post.getTopics().forEach(t -> allTags.add(t.getName()));
        }
        if (!allTags.isEmpty()) {
            response.setTopics(new java.util.ArrayList<>(allTags));
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

        // Reactions breakdown
        java.util.Map<String, Long> reactionCounts = new java.util.HashMap<>();
        for (ReactionType type : ReactionType.values()) {
            long count = reactionRepository.countByPostIdAndType(post.getId(), type);
            if (count > 0) {
                reactionCounts.put(type.name(), count);
            }
        }
        response.setReactionCounts(reactionCounts);

        // Current User Reaction
        if (currentUsername != null) {
            Optional<User> userOpt = userRepository.findByUsername(currentUsername);
            if (userOpt.isPresent()) {
                Optional<Reaction> userReact = reactionRepository.findByUserAndPost(userOpt.get(), post);
                if (userReact.isPresent()) {
                    response.setCurrentUserReaction(userReact.get().getType().name());
                }
            }
        }

        return response;
    }

    // ================================================
    // Private Helper Methods
    // ================================================

    /**
     * Kiểm tra viewer có quyền xem bài viết không.
     * Logic 2 lớp:
     *   Lớp 1 - Khóa trang cá nhân (isPrivate): nếu tác giả khóa trang → phải là bạn bè mới xem được
     *   Lớp 2 - Visibility từng bài (FRIENDS_ONLY): phải là bạn bè mới xem được
     */
    public boolean canUserViewPost(User viewer, Post post) {
        User author = post.getAuthor();
        if (author == null) return true;

        // Chủ bài luôn xem được bài của mình
        if (viewer != null && author.getId().equals(viewer.getId())) return true;

        // [BR-Block] Lớp 0 — BLOCK CHECK: Nếu tồn tại block theo bất kỳ chiều nào → ẩn bài
        // Đây là lớp bảo vệ cao nhất, đặt trước tất cả các kiểm tra khác.
        // isBlockedBetween(A, B) = true nếu A block B HOẶC B block A.
        // viewer = null (guest) không thể bị block → skip check khi viewer là null.
        if (viewer != null && userBlockService.isBlockedBetween(viewer, author)) {
            return false; // Ẩn hoàn toàn bài viết nếu có quan hệ block
        }

        // Kiểm tra bạn bè (nếu viewer = null → areFriends = false tự động)
        boolean areFriends = (viewer != null) && friendshipRepository.isAcceptedFriends(viewer, author);

        // Lớp 1: Tác giả khóa trang → phải là bạn bè
        if (author.isPrivate() && !areFriends) return false;

        // Lớp 2: Bài FRIENDS_ONLY → phải là bạn bè
        if (post.getVisibility() == Visibility.FRIENDS_ONLY && !areFriends) return false;

        return true;
    }

    /**
     * Trích xuất danh sách username được @mention trong nội dung.
     */
    private java.util.List<String> parseMentionedUsernames(String content) {
        java.util.List<String> mentions = new java.util.ArrayList<>();
        if (content == null) return mentions;
        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("@([a-zA-Z0-9_]+)").matcher(content);
        while (m.find()) {
            mentions.add(m.group(1));
        }
        return mentions;
    }

    /**
     * Loại bỏ ký tự @ khỏi nội dung: "@username" → "username".
     */
    private String removeMentionSymbols(String content) {
        if (content == null) return null;
        return java.util.regex.Pattern.compile("@([a-zA-Z0-9_]+)")
                .matcher(content).replaceAll("$1");
    }

    /**
     * Cập nhật điểm quan tâm (UserInterest) cho user theo từng topic.
     */
    private void addUserInterestScore(User user, java.util.List<Topic> topics, int weight) {
        if (topics == null || topics.isEmpty()) return;
        for (Topic topic : topics) {
            UserInterest interest = userInterestRepository.findByUserAndTopic(user, topic)
                    .orElse(new UserInterest(user, topic, 0));
            interest.setScore(interest.getScore() + weight);
            userInterestRepository.save(interest);
        }
    }
}