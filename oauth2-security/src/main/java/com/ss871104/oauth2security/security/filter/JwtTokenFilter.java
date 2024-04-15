package com.ss871104.oauth2security.security.filter;

import com.ss871104.oauth2security.security.token.TokenData;
import com.ss871104.oauth2security.security.token.AccessTokenUtil;
import com.ss871104.oauth2security.security.redis.TokenRedisService;
import com.ss871104.oauth2security.security.handler.CustomAuthenticationException;
import com.ss871104.oauth2security.util.GlobalFunctions;
import com.ss871104.oauth2security.security.token.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
    @Autowired
    private OrRequestMatcher whiteListMatcher;
    @Autowired
    private TokenRedisService tokenRedisService;
    @Autowired
    private AuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AccessTokenUtil accessTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String requestUrl = request.getHeader("X-Forwarded-Uri") != null ? request.getHeader("X-Forwarded-Uri") : request.getRequestURI();
        String requestId = request.getRequestId();

        logger.info("Request ID: {}, Incoming request: {} enter authentication filter...", requestId, requestUrl);

        if (whiteListMatcher.matches(request)) {
            logger.info("Request ID: {}, Request url: {} in white list...", requestId, requestUrl);
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = accessTokenUtil.getAccessTokenFromCookie(request);

        try {
            TokenData tokenData = null;
            String oAuth2Id = "";

            if (StringUtils.hasText(accessToken)) {
                try {
                    tokenData = tokenRedisService.getTokenDataByAccessToken(accessToken);
                } catch (Exception e) {
                    logger.warn("Request ID: {}, Request: {} encounter Redis Error!", requestId, requestUrl);
                    throw new CustomAuthenticationException("Sorry, server is not available at the moment, please try later!");
                }

                if (tokenData == null) {
                    String refreshToken = "";

                    try {
                        refreshToken = tokenRedisService.getRefreshTokenByAccessToken(accessToken);
                    } catch (Exception e) {
                        logger.warn("Request ID: {}, Request: {} encounter Redis Error!", requestId, requestUrl);
                        throw new CustomAuthenticationException("Sorry, server is not available at the moment, please try later!");
                    }

                    if (refreshToken != null) {
                        logger.info("Request ID: {}, Request: {} requires Access Token Refreshing", requestId, requestUrl);

                        Claims claims = jwtUtil.extractAllClaims(refreshToken);
                        tokenData = new TokenData();
                        tokenData.setProvider(claims.getSubject());
                        tokenData.setRoles(claims.get("roles").toString());
                        tokenData.setProvider(claims.get("provider").toString());
                        tokenData.setDevice(claims.get("device").toString());
                        tokenData.setHost(claims.get("host").toString());

                        oAuth2Id = tokenData.getOAuth2Id();

                        String newAccessToken = "";
                        String newRefreshToken = "";
                        try {
                            // Delete current Refresh Token to reset expire time
                            tokenRedisService.removeAccessByAccessToken(oAuth2Id, accessToken);
                            // Generate new Access Token and Refresh Token
                            newAccessToken = accessTokenUtil.generateAccessToken();
                            newRefreshToken = jwtUtil.generateToken(tokenData);
                            tokenRedisService.storeTokenDataByAccessToken(tokenData, newAccessToken);
                            tokenRedisService.storeRefreshTokenByAccessToken(oAuth2Id, newAccessToken, newRefreshToken);
                            tokenRedisService.storeAccessTokenByRefreshToken(accessToken, refreshToken);
                        } catch (Exception e) {
                            logger.warn("Request ID: {}, Request: {} encounter Redis Error!", requestId, requestUrl);
                            throw new CustomAuthenticationException("Sorry, server is not available at the moment, please try later!");
                        }

                        Cookie accessTokenCookie = new Cookie("Auth", newAccessToken);
                        accessTokenCookie.setHttpOnly(true);
                        accessTokenCookie.setSecure(true);
                        accessTokenCookie.setPath("/");

                        response.addCookie(accessTokenCookie);
                    } else {
                        logger.warn("Request ID: {}, Request: {} Refresh Token Expired", requestId, requestUrl);
                        throw new CustomAuthenticationException("Session Time Out, Please Login Again!");
                    }
                } else {
                    oAuth2Id = tokenData.getOAuth2Id();
                }
            } else {
                logger.warn("Request ID: {}, Request: {} Access Token Not Found", requestId, requestUrl);
                throw new CustomAuthenticationException("Please Login!");
            }

            response.setHeader("X-Auth-User", oAuth2Id);

            List<SimpleGrantedAuthority> authorities = new GlobalFunctions().convertStringToAuthorities(tokenData.getRoles());
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(oAuth2Id, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            logger.info("Request ID: {}, Request: {} pass authentication filter...", requestId, requestUrl);
        } catch (CustomAuthenticationException e) {
            authenticationEntryPoint.commence(request, response, new AuthenticationException(e.getMessage()) {});
            return;
        }

        filterChain.doFilter(request, response);
    }

}
