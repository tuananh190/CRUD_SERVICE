package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.PostCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.PostResponse;
import com.mar.CRUD_SERVICE.dto.response.CommentResponse;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.model.User;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import com.mar.CRUD_SERVICE.repository.UserRepository;
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

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
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
        Post saved = postRepository.save(post);
        return mapToResponse(saved);
    }

    public PostResponse updatePost(Long id, PostCreationRequest request) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            // Không thay author ở đây (hoặc có thể cho phép nếu cần)
            Post updated = postRepository.save(post);
            return mapToResponse(updated);
        }
        return null;
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    private PostResponse mapToResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCreatedAt(post.getCreatedAt());
        if (post.getAuthor() != null) {
            PostResponse.UserInfo userInfo = new PostResponse.UserInfo();
            userInfo.setId(post.getAuthor().getId());
            userInfo.setUsername(post.getAuthor().getUsername());
            userInfo.setFirstName(post.getAuthor().getFirstName());
            userInfo.setLastName(post.getAuthor().getLastName());
            response.setAuthor(userInfo);
        }
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
                cr.setCreatedAt(null); // if you have createdAt in entity, map it
                return cr;
            }).collect(Collectors.toList());
            response.setComments(comments);
        }
        return response;
    }
}