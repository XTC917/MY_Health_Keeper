package com.healthkeeper.repository;

import com.healthkeeper.entity.Moment;
import com.healthkeeper.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Long> {
    List<Moment> findByUserOrderByCreatedAtDesc(User user);
    List<Moment> findAllByOrderByCreatedAtDesc();

    List<Moment> findByUserIdInOrderByCreatedAtDesc(Set<Long> friendIds);
} 