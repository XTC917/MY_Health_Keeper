package com.healthkeeper.controller;

import com.healthkeeper.dto.JwtResponse;
import com.healthkeeper.dto.LoginRequest;
import com.healthkeeper.dto.MessageResponse;
import com.healthkeeper.dto.RegisterRequest;
import com.healthkeeper.dto.RegisterResponse;
import com.healthkeeper.entity.ERole;
import com.healthkeeper.entity.Role;
import com.healthkeeper.entity.User;
import com.healthkeeper.repository.RoleRepository;
import com.healthkeeper.repository.UserRepository;
import com.healthkeeper.security.jwt.JwtUtils;
import com.healthkeeper.security.services.UserDetailsImpl;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            logger.info("=== 开始处理登录请求 ===");
            logger.info("登录请求数据: username={}", loginRequest.getUsername());
            
            // 查找用户
            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> {
                    logger.warn("用户不存在: {}", loginRequest.getUsername());
                    return new RuntimeException("用户不存在");
                });
            logger.info("找到用户: {}", user);
            
            // 验证密码
            boolean passwordMatches = encoder.matches(loginRequest.getPassword(), user.getPassword());
            logger.info("密码验证结果: {}", passwordMatches);
            
            if (!passwordMatches) {
                logger.warn("密码不正确");
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 密码不正确!"));
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            logger.info("认证成功");

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);
            logger.info("JWT令牌生成成功");

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            logger.info("用户角色: {}", roles);

            logger.info("=== 登录请求处理完成 ===");
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles));
        } catch (Exception e) {
            logger.error("登录过程中发生错误", e);
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("登录错误: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest signUpRequest) {
        try {
            logger.info("=== 开始处理注册请求 ===");
            logger.info("注册请求数据: username={}, email={}, phone={}", 
                signUpRequest.getUsername(), 
                signUpRequest.getEmail(),
                signUpRequest.getPhone());
            
            // 检查数据库连接
            try {
                long userCount = userRepository.count();
                logger.info("数据库连接测试成功，当前用户数: {}", userCount);
            } catch (Exception e) {
                logger.error("数据库连接测试失败", e);
                return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("数据库连接错误: " + e.getMessage()));
            }

            // 检查用户名是否存在
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                logger.warn("用户名已存在: {}", signUpRequest.getUsername());
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 用户名已被使用!"));
            }

            // 检查邮箱是否存在
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                logger.warn("邮箱已被使用: {}", signUpRequest.getEmail());
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("错误: 邮箱已被使用!"));
            }

            // 创建新用户
            logger.info("开始创建新用户对象");
            User user = new User();
            user.setUsername(signUpRequest.getUsername());
            user.setEmail(signUpRequest.getEmail());
            user.setPassword(encoder.encode(signUpRequest.getPassword()));
            user.setPhone(signUpRequest.getPhone());
            
            // 设置创建时间
            LocalDateTime now = LocalDateTime.now();
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            logger.info("用户对象创建完成: {}", user);

            // 获取或创建用户角色
            logger.info("开始处理用户角色");
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                .orElseGet(() -> {
                    logger.info("创建新的 ROLE_USER 角色");
                    Role newRole = new Role(ERole.ROLE_USER);
                    return roleRepository.save(newRole);
                });
            logger.info("用户角色处理完成: {}", userRole);

            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);

            // 保存用户
            logger.info("开始保存用户到数据库");
            try {
                User savedUser = userRepository.save(user);
                logger.info("用户保存成功，ID: {}", savedUser.getId());
                
                // 验证保存是否成功
                User verifiedUser = userRepository.findById(savedUser.getId())
                    .orElseThrow(() -> {
                        logger.error("用户保存验证失败，ID: {}", savedUser.getId());
                        return new RuntimeException("用户未正确保存");
                    });
                logger.info("用户验证成功: {}", verifiedUser);
                
                RegisterResponse response = new RegisterResponse(
                    "用户注册成功!",
                    verifiedUser.getId(),
                    verifiedUser.getUsername(),
                    verifiedUser.getEmail()
                );
                logger.info("=== 注册请求处理完成 ===");
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("保存用户到数据库时发生错误", e);
                logger.error("错误详情:", e);
                return ResponseEntity
                    .internalServerError()
                    .body(new MessageResponse("保存用户错误: " + e.getMessage()));
            }
        } catch (Exception e) {
            logger.error("注册过程中发生意外错误", e);
            logger.error("错误详情:", e);
            return ResponseEntity
                .internalServerError()
                .body(new MessageResponse("注册错误: " + e.getMessage()));
        }
    }
} 