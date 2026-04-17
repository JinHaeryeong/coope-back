package com.coope.server.community.domain.post;

import com.coope.server.community.domain.post.enums.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface PostRepository {
    Optional<Post> findById(Long id);
    Post save(Post post);
    void delete(Post post);
    Page<Post> findAllWithAuthor(Pageable pageable);
    Page<Post> findByCategoryWithAuthor(PostCategory category, Pageable pageable);
    void incrementCommentCount(Long id);
    void decrementCommentCount(Long id);
    Page<Post> searchByKeyword(String keyword, Pageable pageable);
    Page<Post> searchByCategoryAndKeyword(PostCategory category, String keyword, Pageable pageable);
}
