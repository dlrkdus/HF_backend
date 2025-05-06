package com.hf.healthfriend.domain.comment.repository;

import com.hf.healthfriend.domain.comment.entity.Comment;
import com.hf.healthfriend.domain.comment.repository.querydsl.CommentCustomRepository;
import com.hf.healthfriend.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentJpaRepository extends JpaRepository<Comment, Long>, CommentCustomRepository {

    @Query("""
            SELECT c
            FROM Comment c
            WHERE c.post.postId = :postId
                AND c.isDeleted = FALSE
            """)
    List<Comment> findByPostId(@Param("postId") long postId);

    @Query("""
            SELECT c
            FROM Comment c
            WHERE c.writer.id = :writerId
                AND c.isDeleted = FALSE
            """)
    List<Comment> findByWriterId(@Param("writerId") long writerId);

    boolean existsByCommentIdAndIsDeletedFalse(Long id);

    @Modifying
    @Query(value = "WITH RECURSIVE CommentHierarchy AS (" +
            "  SELECT comment_id " +
            "  FROM comment " +
            "  WHERE comment_id = :parentId " +
            "  UNION ALL " +
            "  SELECT c.comment_id " +
            "  FROM comment c " +
            "  INNER JOIN CommentHierarchy ch ON c.parent_comment_id = ch.comment_id" +
            ") " +
            "UPDATE comment " +
            "SET is_deleted = true " +
            "WHERE comment_id IN (SELECT comment_id FROM CommentHierarchy)",
            nativeQuery = true)
    void deleteAllReplies(@Param("parentId") Long parentId);

    @Modifying
    @Query("UPDATE Comment c SET c.isDeleted = true WHERE c.commentId = :commentId")
    void softDeleteById(Long commentId);

    @Query("SELECT c FROM Comment c WHERE c.commentId = :commentId AND c.isDeleted=false ")
    Optional<Comment> findByCommentIdAndIsDeletedFalse(Long commentId);

    @Query("SELECT c.post FROM Comment c WHERE c.commentId = :commentId AND c.isDeleted = false ")
    Optional<Post> findPostByCommentIdAndIsDeletedFalse(Long commentId);
}
