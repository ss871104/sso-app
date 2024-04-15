package com.ss871104.oauth2security.controllers;

import com.ss871104.oauth2security.dtos.output.ErrorResponse;
import com.ss871104.oauth2security.security.redis.TokenRedisService;
import com.ss871104.oauth2security.security.token.AccessTokenUtil;
import com.ss871104.oauth2security.security.token.JwtUtil;
import com.ss871104.oauth2security.security.token.TokenData;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth-service/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private TokenRedisService tokenRedisService;
    @Autowired
    private AccessTokenUtil accessTokenUtil;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/authenticate")
    public ResponseEntity authenticate() {
        return new ResponseEntity<>("{\"isAuthenticated\": true}", HttpStatus.OK);
    }

    @GetMapping("/logout")
    public ResponseEntity logout(HttpServletRequest request) {
        String accessToken = accessTokenUtil.getAccessTokenFromCookie(request);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String oAuth2Id = authentication.getPrincipal().toString();
        String requestId = request.getRequestId();

        try {
            tokenRedisService.removeAccessByAccessToken(oAuth2Id, accessToken);
        } catch (Exception e) {
            logger.error("Request ID: {}, Logout Encounter Redis Error by user: {}", requestId, oAuth2Id);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/userAccesses")
    public ResponseEntity fetchUserAccesses(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String oAuth2Id = authentication.getPrincipal().toString();
        String requestId = request.getRequestId();
        Set<String> refreshTokenSet = new HashSet<>();
        List<TokenData> tokenDataList = new ArrayList<>();

        try {
            refreshTokenSet = tokenRedisService.getRefreshTokenSetByOAuth2Id(oAuth2Id);
        } catch (Exception e) {
            logger.error("Request ID: {}, Fetch User Accesses Encounter Redis Error by user: {}", requestId, oAuth2Id);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }
        try {
            refreshTokenSet.forEach(r -> {
                Claims claims = jwtUtil.extractAllClaims(r);
                TokenData tokenData = new TokenData();
                String accessToken = tokenRedisService.getAccessTokenByRefreshToken(r);
                tokenData.setAccessToken(accessToken);
                tokenData.setOAuth2Id(claims.getSubject());
                tokenData.setRoles(claims.get("roles").toString());
                tokenData.setProvider(claims.get("provider").toString());
                tokenData.setDevice(claims.get("device").toString());
                tokenData.setHost(claims.get("host").toString());
                tokenDataList.add(tokenData);
            });
        } catch (Exception e) {
            logger.error("Request ID: {}, Fetch User Accesses Encounter Redis Error by user: {}", requestId, oAuth2Id);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }

        return new ResponseEntity<>(tokenDataList, HttpStatus.OK);
    }

    @DeleteMapping("/userAccess/{accessToken}")
    public ResponseEntity removeUserAccess(HttpServletRequest request, @PathVariable("accessToken") String accessToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String oAuth2Id = authentication.getPrincipal().toString();
        String requestId = request.getRequestId();
        String refreshToken = "";

        try {
            refreshToken = tokenRedisService.getRefreshTokenByAccessToken(accessToken);
            Claims claims = jwtUtil.extractAllClaims(refreshToken);
            String oAuth2IdFromRefreshToken = claims.getSubject();
            if (!oAuth2IdFromRefreshToken.equals(oAuth2Id)) {
                logger.error("Request ID: {}, User: {} is unauthorized to remove other user's: {} access", requestId, oAuth2Id, oAuth2IdFromRefreshToken);
                ErrorResponse errorResponse = new ErrorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
                return new ResponseEntity<>(errorResponse, HttpStatus.OK);
            }
            tokenRedisService.removeAccessByRefreshToken(oAuth2Id, refreshToken);
        } catch (Exception e) {
            logger.error("Request ID: {}, Remove User Access Encounter Redis Error by userId: {}", requestId, oAuth2Id);
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER ERROR");
            return new ResponseEntity<>(errorResponse, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
