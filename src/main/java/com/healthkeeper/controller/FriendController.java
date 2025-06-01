// src/main/java/com/healthkeeper/controller/FriendController.java
package com.healthkeeper.controller;

import com.healthkeeper.dto.FriendDTO;
import com.healthkeeper.dto.FriendRequestDTO;
import com.healthkeeper.entity.FriendRequest;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.FriendRequestRepository;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.context.SecurityContextHolder;
import com.healthkeeper.security.services.UserDetailsImpl;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friends")
public class FriendController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    FriendRequestRepository friendRequestRepository;

    // 发送好友请求
    @PostMapping("/requests")
    @Transactional
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, String> body) {
        // 从 SecurityContext 中获取当前用户
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User sender = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "发送者不存在"));

        // 获取接收方用户名
        String receiverUsername = body.get("username");
        if (receiverUsername == null || receiverUsername.isEmpty()) {
            return ResponseEntity.badRequest().body("用户名不能为空");
        }

        // 检查接收方用户是否存在
        User receiver = userRepository.findByUsername(receiverUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

        // 检查是否已有请求
        List<FriendRequest> existing = friendRequestRepository.findBySenderIdAndReceiverId(
                sender.getId(), receiver.getId()
        );

        /*if (!existing.isEmpty()) {
            return ResponseEntity.badRequest().body("请求已发送");
        }*/

        FriendRequest request = new FriendRequest();
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus("PENDING");
        request.setCreatedAt(LocalDateTime.now());
        friendRequestRepository.save(request);

        return ResponseEntity.ok("请求已发送");
    }

    // 处理好友请求
    @PutMapping("/requests/{id}")
    @Transactional
    public ResponseEntity<FriendRequestDTO> handleRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            FriendRequest request = friendRequestRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "请求不存在"));

            System.out.println("Handling friend request with id=" + id);
            System.out.println("Action: " + body.get("action"));


            if ("ACCEPT".equals(body.get("action"))) {
                // 添加双向好友关系
                User sender = userRepository.findById(request.getSender().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "发送方不存在"));
                User receiver = userRepository.findById(request.getReceiver().getId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "接收方不存在"));

                System.out.println("Sender = " + request.getSender().getUsername());
                System.out.println("Receiver = " + request.getReceiver().getUsername());


                // 重新从数据库加载实体，确保处于同一会话
                sender = userRepository.findById(sender.getId()).orElseThrow();
                receiver = userRepository.findById(receiver.getId()).orElseThrow();



                // 强制初始化延迟加载的关联
                Hibernate.initialize(sender.getFriends());
                Hibernate.initialize(receiver.getFriends());

                // 添加双向关系（Hibernate自动同步）
                sender.getFriends().add(receiver);
                receiver.getFriends().add(sender);

                //userRepository.saveAndFlush(sender);
                //userRepository.saveAndFlush(receiver);

                userRepository.save(sender);
                userRepository.save(receiver);
                userRepository.flush(); // 立即刷新以确保事务提交

                request.setStatus("ACCEPTED");
            } else {
                request.setStatus("REJECTED");
            }

            FriendRequest saved = friendRequestRepository.save(request);

            return ResponseEntity.ok(new FriendRequestDTO(
                    saved.getId(),
                    saved.getSender().getUsername(),
                    saved.getSender().getAvatar(),
                    saved.getReceiver().getUsername(),
                    saved.getReceiver().getAvatar(),
                    saved.getStatus()
            ));
        }catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    // 获取好友列表
    @GetMapping("")
    public ResponseEntity<?> getFriends() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        Hibernate.initialize(user.getFriends()); // 强制初始化延迟加载集合

        List<FriendDTO> friendDTOs = user.getFriends().stream()
                .map(friend -> {
                    FriendDTO dto = new FriendDTO();
                    dto.setId(friend.getId());
                    dto.setUsername(friend.getUsername());
                    dto.setAvatar(friend.getAvatar());
                    return dto;
                }).toList();

        return ResponseEntity.ok(friendDTOs);
    }

    // 获取当前用户收到的好友请求列表
    @GetMapping("/requests")
    public ResponseEntity<?> getFriendRequests() {
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId()).orElseThrow();
        List<FriendRequest> requests = friendRequestRepository.findByReceiverIdAndStatus(user.getId(), "PENDING");
        List<FriendRequestDTO> dtoList = requests.stream().map(req -> new FriendRequestDTO(
                req.getId(),
                req.getSender().getUsername(),
                req.getSender().getAvatar(),
                req.getReceiver().getUsername(),
                req.getReceiver().getAvatar(),
                req.getStatus()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // 在FriendController.java中添加以下代码
    @DeleteMapping("/{friendUsername}")
    @Transactional
    public ResponseEntity<?> removeFriend(@PathVariable String friendUsername) {
        try {
            // 获取当前用户
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User currentUser = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用户不存在"));

            // 获取要删除的好友
            User friendToRemove = userRepository.findByUsername(friendUsername)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "好友不存在"));

            // 双向解除好友关系
            if (currentUser.getFriends().contains(friendToRemove)) {
                currentUser.getFriends().remove(friendToRemove);
                userRepository.save(currentUser); // 触发Hibernate自动同步
            }
            if (friendToRemove.getFriends().contains(currentUser)) {
                friendToRemove.getFriends().remove(currentUser);
                userRepository.save(friendToRemove); // 触发Hibernate自动同步
            }

            return ResponseEntity.ok("好友关系已解除");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("删除失败");
        }
    }
}
