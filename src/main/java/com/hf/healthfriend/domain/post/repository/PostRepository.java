package com.hf.healthfriend.domain.post.repository;

import com.hf.healthfriend.domain.post.entity.Post;
import com.hf.healthfriend.domain.post.repository.querydsl.PostCustomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostCustomRepository {
    Optional<Post> findByPostIdAndIsDeletedFalse(Long id);

    Page<Post> findAll(Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.postId = :postId")
    void incrementLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.likesCount = p.likesCount - 1 WHERE p.postId = " +
            "(SELECT l.post.postId FROM Like l WHERE l.likeId = :likeId)")
    void decrementLikeCountByLikeId(@Param("likeId") Long likeId);

    @Modifying
    @Query("UPDATE Post p SET p.commentsCount = p.commentsCount + 1 WHERE p.postId = :postId")
    void incrementCommentsCount(@Param("postId") Long postId);

    @Modifying
    @Query(value = """
    WITH RECURSIVE CommentHierarchy AS (
        SELECT comment_id
        FROM comment
        WHERE comment_id = :parentId
        UNION ALL
        SELECT c.comment_id
        FROM comment c
        INNER JOIN CommentHierarchy ch ON c.parent_comment_id = ch.comment_id
    )
    UPDATE post
    SET comments_count = comments_count - (
        SELECT COUNT(*) FROM CommentHierarchy
    )
    WHERE post_id = (
        SELECT post_id FROM comment WHERE comment_id = :parentId
    )
    """, nativeQuery = true)
    void decrementCommentsCountByParentCommentId(@Param("parentId") Long parentId);
}
