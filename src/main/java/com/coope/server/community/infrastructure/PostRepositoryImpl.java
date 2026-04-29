package com.coope.server.community.infrastructure;

import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.PostRepository;
import com.coope.server.community.domain.post.enums.PostCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * {@link PostRepository} 도메인 인터페이스의 JPA 구현체
 * Service는 {@code PostRepository} 인터페이스에만 의존하므로
 * 추후 JPA → 다른 기술로 교체하더라도 도메인, 애플리케이션 계층은 변경 없음
 */
@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostJpaRepository postJpaRepository;

    @Override
    public Optional<Post> findById(Long id) {
        return postJpaRepository.findById(id);
    }

    @Override
    public Post save(Post post) {
        return postJpaRepository.save(post);
    }

    @Override
    public void delete(Post post) {
        postJpaRepository.delete(post);
    }

    @Override
    public Page<Post> findAllWithAuthor(Pageable pageable) {
        return postJpaRepository.findAllWithAuthor(pageable);
    }

    @Override
    public Page<Post> findByCategoryWithAuthor(PostCategory category, Pageable pageable) {
        return postJpaRepository.findByCategoryWithAuthor(category, pageable);
    }

    @Override
    public void incrementCommentCount(Long id) {
        postJpaRepository.incrementCommentCount(id);
    }

    @Override
    public void decrementCommentCount(Long id) {
        postJpaRepository.decrementCommentCount(id);
    }

    @Override
    public void incrementLikeCount(Long id) {
        postJpaRepository.incrementLikeCount(id);
    }

    @Override
    public void decrementLikeCount(Long id) {
        postJpaRepository.decrementLikeCount(id);
    }

    @Override
    public Page<Post> findTopByLikeCount(Pageable pageable) {
        return postJpaRepository.findTopByLikeCount(pageable);
    }

    @Override
    public Page<Post> searchByKeyword(String keyword, Pageable pageable) {
        return postJpaRepository.searchByKeyword(keyword, pageable);
    }

    @Override
    public Page<Post> searchByCategoryAndKeyword(PostCategory category, String keyword, Pageable pageable) {
        return postJpaRepository.searchByCategoryAndKeyword(category, keyword, pageable);
    }
}
