package com.mar.CRUD_SERVICE.service;

import com.mar.CRUD_SERVICE.model.Hashtag;
import com.mar.CRUD_SERVICE.model.Post;
import com.mar.CRUD_SERVICE.repository.HashtagRepository;
import com.mar.CRUD_SERVICE.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    private final PostRepository postRepository;

    public HashtagService(HashtagRepository hashtagRepository, PostRepository postRepository) {
        this.hashtagRepository = hashtagRepository;
        this.postRepository = postRepository;
    }

    public List<Hashtag> getAllHashtags() {
        return hashtagRepository.findAll();
    }

    public List<Post> getPostsByHashtag(String tag) {
        Hashtag hashtag = hashtagRepository.findByName(tag)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hashtag: #" + tag));
        return hashtag.getPosts();
    }

    public String addHashtagToPost(Long postId, String tag) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));

        Hashtag hashtag = hashtagRepository.findByName(tag)
                .orElseGet(() -> hashtagRepository.save(new Hashtag(tag)));

        if (post.getHashtags() != null && post.getHashtags().contains(hashtag)) {
            throw new IllegalStateException("Bài viết đã có hashtag #" + tag + " rồi.");
        }

        post.getHashtags().add(hashtag);
        postRepository.save(post);
        return "Đã thêm #" + tag + " vào bài viết.";
    }

    public String removeHashtagFromPost(Long postId, String tag) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài viết ID: " + postId));
        Hashtag hashtag = hashtagRepository.findByName(tag)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hashtag: #" + tag));

        boolean removed = post.getHashtags().remove(hashtag);
        if (!removed) {
            throw new IllegalStateException("Bài viết không có hashtag #" + tag);
        }
        postRepository.save(post);
        return "Đã xoá #" + tag + " khỏi bài viết.";
    }
}
