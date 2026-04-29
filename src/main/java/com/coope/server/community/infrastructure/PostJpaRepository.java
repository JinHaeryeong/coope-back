package com.coope.server.community.infrastructure;

import com.coope.server.community.domain.post.Post;
import com.coope.server.community.domain.post.enums.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostJpaRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.id DESC")
    Page<Post> findAllWithAuthor(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.category = :category ORDER BY p.id DESC")
    Page<Post> findByCategoryWithAuthor(@Param("category") PostCategory category, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.id = :id AND p.commentCount > 0")
    void decrementCommentCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.id = :id AND p.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);

    @Query("SELECT p FROM Post p JOIN FETCH p.author ORDER BY p.likeCount DESC, p.id DESC")
    Page<Post> findTopByLikeCount(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% ORDER BY p.id DESC")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.author WHERE p.category = :category AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) ORDER BY p.id DESC")
    Page<Post> searchByCategoryAndKeyword(@Param("category") PostCategory category, @Param("keyword") String keyword, Pageable pageable);
}
