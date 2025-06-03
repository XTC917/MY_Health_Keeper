package com.healthkeeper.repository;

import com.healthkeeper.entity.Comment;
import com.healthkeeper.entity.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByMomentAndParentIsNullOrderByCreatedAtDesc(Moment moment);
    List<Comment> findByParentOrderByCreatedAtAsc(Comment parent);
} 