package com.ss871104.oauth2security.security.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss871104.oauth2security.security.token.TokenData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class TokenRedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public void storeTokenDataByAccessToken(TokenData tokenData, String accessToken) throws JsonProcessingException {
        String tokenDataJson = objectMapper.writeValueAsString(tokenData);
        String accessTokenKey = "access_for_data:" + accessToken;
        redisTemplate.opsForValue().set(accessTokenKey, tokenDataJson);
        redisTemplate.expire(accessTokenKey, 30, TimeUnit.MINUTES);
    }

    public void storeRefreshTokenByAccessToken(String oAuth2Id, String accessToken, String refreshToken) {
        String refreshTokenKey = "access_for_refresh:" + accessToken;
        String refreshTokenSetKey = "refresh_token_set:" + oAuth2Id;
        redisTemplate.opsForValue().set(refreshTokenKey, refreshToken);
        redisTemplate.expire(refreshTokenKey, 15, TimeUnit.DAYS);

        redisTemplate.opsForSet().add(refreshTokenSetKey, refreshToken);
        redisTemplate.expire(refreshTokenSetKey, 15, TimeUnit.DAYS);
    }

    public void storeAccessTokenByRefreshToken(String accessToken, String refreshToken) {
        String accessTokenKey = "refresh_for_access:" + refreshToken;
        redisTemplate.opsForValue().set(accessTokenKey, accessToken);
        redisTemplate.expire(accessTokenKey, 15, TimeUnit.DAYS);
    }

    public TokenData getTokenDataByAccessToken(String accessToken) throws JsonProcessingException {
        String tokenDataJson = redisTemplate.opsForValue().get("access_for_data:" + accessToken);
        if (tokenDataJson == null) {
            return null;
        }
        return objectMapper.readValue(tokenDataJson, TokenData.class);
    }

    public String getRefreshTokenByAccessToken(String accessToken) {
        String key = "access_for_refresh:" + accessToken;
        return redisTemplate.opsForValue().get(key);
    }

    public String getAccessTokenByRefreshToken(String refreshToken) {
        String key = "refresh_for_access:" + refreshToken;
        return redisTemplate.opsForValue().get(key);
    }

    public Set<String> getRefreshTokenSetByOAuth2Id(String oAuth2Id) {
        Set<String> refreshTokenSet = redisTemplate.opsForSet().members("refresh_token_set:" + oAuth2Id);
        if (refreshTokenSet == null || refreshTokenSet.size() == 0) {
            return null;
        }
        return refreshTokenSet;
    }

    public void removeAccessByAccessToken(String oAuth2Id, String accessToken) {
        String refreshToken = getRefreshTokenByAccessToken(accessToken);
        redisTemplate.delete("access_for_data:" + accessToken);
        redisTemplate.delete("access_for_refresh:" + accessToken);
        if (refreshToken != null) {
            redisTemplate.delete("refresh_for_access:" + refreshToken);
            redisTemplate.opsForSet().remove("refresh_token_set:" + oAuth2Id, refreshToken);
        }
    }

    public void removeAccessByRefreshToken(String oAuth2Id, String refreshToken) {
        String accessToken = getAccessTokenByRefreshToken(refreshToken);
        redisTemplate.delete("refresh_for_access:" + refreshToken);
        redisTemplate.opsForSet().remove("refresh_token_set:" + oAuth2Id, refreshToken);
        if (accessToken != null) {
            redisTemplate.delete("access_for_data:" + accessToken);
            redisTemplate.delete("access_for_refresh:" + accessToken);
        }
    }

    public void removeAllAccessesByOAuth2Id(String oAuth2Id) {
        Set<String> refreshTokenSet = getRefreshTokenSetByOAuth2Id(oAuth2Id);
        refreshTokenSet.forEach(r -> {
            String accessToken = getAccessTokenByRefreshToken(r);
            redisTemplate.delete("refresh_for_access:" + r);
            redisTemplate.opsForSet().remove("refresh_token_set:" + oAuth2Id, r);
            if (accessToken != null) {
                redisTemplate.delete("access_for_data:" + accessToken);
                redisTemplate.delete("access_for_refresh:" + accessToken);
            }
        });
    }
}
