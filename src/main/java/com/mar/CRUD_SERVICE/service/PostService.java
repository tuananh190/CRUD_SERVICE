package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.dto.request.PostCreationRequest;
import com.mar.CRUD_SERVICE.dto.response.PostResponse;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;

    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
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
        // Nếu có các trường khác, hãy set thêm ở đây
        Post saved = postRepository.save(post);
        return mapToResponse(saved);
    }

    public PostResponse updatePost(Long id, PostCreationRequest request) {
        Optional<Post> optionalPost = postRepository.findById(id);
        if (optionalPost.isPresent()) {
            Post post = optionalPost.get();
            post.setTitle(request.getTitle());
            post.setContent(request.getContent());
            // Nếu có các trường khác, hãy cập nhật thêm ở đây
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
        // Nếu có các trường khác, hãy map thêm ở đây
        return response;
    }
}