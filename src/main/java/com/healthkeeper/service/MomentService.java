package com.healthkeeper.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthkeeper.entity.Moment;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.MomentRepository;
import com.healthkeeper.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Data
@Service
public class MomentService {

    @Autowired
    private MomentRepository momentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 点赞动态
     */
    @Transactional
    public Moment likeMoment(Long momentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));

        if (!moment.getLikes().contains(user)) {
            moment.getLikes().add(user);
        }
        //moment.setLikesCount(moment.getLikes().size()); // 👈 手动更新点赞数
        return momentRepository.save(moment);
    }

    /**
     * 取消点赞动态
     */
    @Transactional
    public Moment unlikeMoment(Long momentId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));

        if (moment.getLikes().contains(user)) {
            moment.getLikes().remove(user);
        }
        //moment.setLikesCount(moment.getLikes().size()); // 👈 同步更新
        return momentRepository.save(moment);
    }
}
