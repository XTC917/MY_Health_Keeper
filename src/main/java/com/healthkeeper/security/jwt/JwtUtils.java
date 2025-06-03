/**
 * AI-generated-content
 * tool: ChatGPT
 * version: 3.5
 * usage: I used the prompt "generate a JwtUtils class for JWT token handling in Spring Security" and
 * directly copied the code from its response.
 */

package com.healthkeeper.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.springframework.security.core.GrantedAuthority;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        logger.debug("Generating JWT token for user: {}", userPrincipal.getUsername());

        // 添加用户角色声明
        Claims claims = Jwts.claims().setSubject(userPrincipal.getUsername());
        claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();

        logger.debug("Generated JWT token with roles: {}", claims.get("roles"));
        return token;
    }

    public String getUserNameFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            logger.debug("Extracted username from token: {}", claims.getSubject());
            logger.debug("Token roles: {}", claims.get("roles"));
            
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken)
                .getBody();

            // 检查令牌是否过期
            if (claims.getExpiration().before(new Date())) {
                logger.error("JWT token is expired");
                return false;
            }

            // 检查是否包含必要的声明
            if (claims.getSubject() == null || claims.get("roles") == null) {
                logger.error("JWT token missing required claims");
                return false;
            }

            logger.debug("JWT token validation successful for user: {}", claims.getSubject());
            logger.debug("Token roles: {}", claims.get("roles"));
            
            return true;
        } catch (SecurityException e) {
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }
} 