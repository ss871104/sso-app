package com.ss871104.oauth2security.security.filter;

import com.ss871104.oauth2security.models.Permission;
import com.ss871104.oauth2security.repositories.PermissionRepository;
import com.ss871104.oauth2security.security.redis.PermissionRedisService;
import com.ss871104.oauth2security.util.CustomHttpServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CustomAuthorizationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthorizationFilter.class);
    @Autowired
    private OrRequestMatcher whiteListMatcher;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private AccessDeniedHandler accessDeniedHandler;
    @Autowired
    private PermissionRedisService permissionRedisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (whiteListMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUrl = request.getHeader("X-Forwarded-Uri") != null ? request.getHeader("X-Forwarded-Uri") : request.getRequestURI();
        String requestId = request.getRequestId();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            if (authentication != null && authentication.isAuthenticated()) {
                String oAuth2Id = authentication.getPrincipal().toString();

                logger.info("Request ID: {}, Incoming request: {} enter authorization filter by user: {}...", requestId, requestUrl, oAuth2Id);

                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                Set<String> roleNames = authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());

                Set<String> allowedUrls = new HashSet<>();

                try {
                    allowedUrls = permissionRedisService.getAllowPermissionUrlsByRoleNames(roleNames);
                } catch (Exception e) {
                    logger.warn("Request ID: {}, Request: {} encounter Redis Error!", requestId, requestUrl);
                    throw new AuthorizationServiceException("Sorry, server is not available at the moment, please try later!");
                }

                if (allowedUrls == null) {
                    Set<Permission> permissions = new HashSet<>();

                    try {
                        permissions = permissionRepository.findPermissionsByRoleNames(roleNames);
                    } catch (Exception e) {
                        logger.warn("Request ID: {}, Request: {} encounter DB Error!", requestId, requestUrl);
                        throw new AuthorizationServiceException("Sorry, server is not available at the moment, please try later!");
                    }

                    if (permissions.size() == 0) {
                        logger.warn("Request ID: {}, User: {} has no permission to any url", requestId, oAuth2Id);
                        throw new AuthorizationServiceException("Unauthorized: No permission");
                    }
                    allowedUrls = permissions.stream()
                            .map(Permission::getUrl)
                            .collect(Collectors.toSet());
                }

                try {
                    permissionRedisService.storeAllowPermissionUrlsByRoleNames(roleNames, allowedUrls);
                } catch (Exception e) {
                    logger.warn("Request ID: {}, Request: {} encounter Redis Error!", requestId, requestUrl);
                    throw new AuthorizationServiceException("Sorry, server is not available at the moment, please try later!");
                }

                OrRequestMatcher allowedUrlsMatcher = new OrRequestMatcher(allowedUrls.stream()
                        .map(AntPathRequestMatcher::new)
                        .collect(Collectors.toList()));

                HttpServletRequest wrappedRequest = new CustomHttpServletRequestWrapper(request, requestUrl);

                if (!allowedUrlsMatcher.matches(wrappedRequest)) {
                    logger.warn("Request ID: {}, Request: {} is not allowed for user: {}", requestId, requestUrl, oAuth2Id);
                    throw new AuthorizationServiceException("Unauthorized: Access not allowed");
                }
                logger.info("Request ID: {}, Request: {} pass authorization filter by user: {}...", requestId, requestUrl, oAuth2Id);
            } else {
                logger.warn("Request ID: {}, Request: {} authentication not found", requestId, requestUrl);
                throw new AuthorizationServiceException("Unauthorized: Access not allowed");
            }
        } catch (AuthorizationServiceException e) {
            accessDeniedHandler.handle(request, response, new AuthorizationServiceException(e.getMessage()) {});
            return;
        }

        filterChain.doFilter(request, response);
    }
}
