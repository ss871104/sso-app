package com.ss871104.oauth2security.security.handler;

import com.ss871104.oauth2security.models.Provider;
import com.ss871104.oauth2security.models.Role;
import com.ss871104.oauth2security.models.AuthUser;
import com.ss871104.oauth2security.repositories.AuthUserRepository;
import com.ss871104.oauth2security.repositories.RoleRepository;
import com.ss871104.oauth2security.security.kafka.Oauth2Data;
import com.ss871104.oauth2security.security.token.TokenData;
import com.ss871104.oauth2security.security.token.AccessTokenUtil;
import com.ss871104.oauth2security.security.redis.TokenRedisService;
import com.ss871104.oauth2security.util.GlobalFunctions;
import com.ss871104.oauth2security.security.token.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Transactional(rollbackFor = CustomAuthenticationException.class)
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    @Value("${frontend.url.origin}")
    private String frontendUrl;
    @Autowired
    private AuthUserRepository authUserRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AccessTokenUtil accessTokenUtil;
    @Autowired
    private TokenRedisService tokenRedisService;
    @Autowired
    private GlobalFunctions globalFunctions;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        String oAuth2Id = "";
        Set<Role> roleSet = new HashSet<>();
        String device = request.getHeader("User-Agent");
        String host = request.getHeader("Host");
        String requestId = request.getRequestId();

        Provider provider = switch (oauthToken.getAuthorizedClientRegistrationId()) {
            case "google" -> Provider.GOOGLE;
            case "facebook" -> Provider.FACEBOOK;
            default -> null;
        };

        if (provider != null) {
            oAuth2Id = switch (provider.name()) {
                case "GOOGLE" -> oAuth2User.getAttribute("sub");
                case "FACEBOOK" -> oAuth2User.getAttribute("id");
                default -> null;
            };
        } else {
            logger.warn("Request ID: {}, Provider not found, fail to login", requestId);
            throw new CustomAuthenticationException("Server Error, Please Login Later!");
        }

        if (oAuth2Id != null) {
            logger.info("Request ID: {}, Login Success! Processing AuthenticationSuccessHandler, oAuth2Id: {}, provider: {}", requestId, oAuth2Id, provider.name());

            AuthUser authUser = new AuthUser();

            try {
                authUser = authUserRepository.findAuthUserByOAuth2Id(oAuth2Id);
            } catch (Exception e) {
                logger.warn("Request ID: {}, DB Error! Fail to save user: {}", requestId, oAuth2Id);
                throw new CustomAuthenticationException("Server Error, Please Login Later!");
            }
            // Check if user have login before, if not, store data into db
            if (authUser == null) {
                logger.info("Request ID: {}, Welcome new user: {}, storing data into db...", requestId, oAuth2Id);

                authUser = new AuthUser();
                Role role = new Role();

                authUser.setoAuth2Id(oAuth2Id);
                authUser.setProvider(provider);
                authUser.setBlocked(false);

                try {
                    // ROLE_USER as default
                    role = roleRepository.findRoleById(1L);
                } catch (Exception e) {
                    logger.warn("Request ID: {}, DB Error! Fail to save user: {}", requestId, oAuth2Id);
                    throw new CustomAuthenticationException("Server Error, Please Login Later!");
                }

                roleSet.add(role);
                authUser.setRoles(roleSet);

                try {
                    authUserRepository.save(authUser);
                    logger.info("Request ID: {}, New User: {} saved...", requestId, oAuth2Id);
                } catch (Exception e) {
                    logger.warn("Request ID: {}, DB Error! Fail to save user: {}", requestId, oAuth2Id);
                    throw new CustomAuthenticationException("Server Error, Please Login Later!");
                }

                try {
                    Oauth2Data oauth2Data = new Oauth2Data();
                    oauth2Data.setOAuth2Id(oAuth2Id);
                    oauth2Data.setProvider(provider.name());
                    oauth2Data.setEmail(oAuth2User.getAttribute("email"));
                    oauth2Data.setFullName(oAuth2User.getAttribute("name"));
                    oauth2Data.setFamilyName(oAuth2User.getAttribute("family_name"));
                    oauth2Data.setGivenName(oAuth2User.getAttribute("given_name"));

                    kafkaTemplate.send("oauth2-data", globalFunctions.convertAnythingToString(oauth2Data));
                    logger.info("Request ID: {}, data of New User: {} sent by Kafka...", requestId, oAuth2Id);
                } catch (Exception e) {
                    logger.warn("Request ID: {}, Kafka Error! Failed to send data of user: {}", requestId, oAuth2Id);
                    throw new CustomAuthenticationException("Server Error, Please Login Later!");
                }

            } else {
                if (authUser.isBlocked()) {
                    logger.warn("Request ID: {}, Access Denied to blocked user: {}...", requestId, oAuth2Id);
                    throw new CustomAuthenticationException("The account has been blocked, please contact system admin!");
                }
                logger.info("Request ID: {}, Welcome user: {}...", requestId, oAuth2Id);
                roleSet = authUser.getRoles();
            }

            TokenData tokenData = new TokenData();
            tokenData.setOAuth2Id(oAuth2Id);
            tokenData.setRoles(globalFunctions.convertRolesToroleNamesString(roleSet));
            tokenData.setProvider(provider.name());
            tokenData.setDevice(device);
            tokenData.setHost(host);

            String accessToken = accessTokenUtil.generateAccessToken();
            String refreshToken = jwtUtil.generateToken(tokenData);

            try {
                tokenRedisService.storeTokenDataByAccessToken(tokenData, accessToken);
                tokenRedisService.storeRefreshTokenByAccessToken(oAuth2Id, accessToken, refreshToken);
                tokenRedisService.storeAccessTokenByRefreshToken(accessToken, refreshToken);
            } catch (Exception e) {
                logger.warn("Request ID: {}, Redis Error! Fail to save token for user: {}", requestId, oAuth2Id);
                throw new CustomAuthenticationException("Server Error, Please Login Later!");
            }

            Cookie accessTokenCookie = new Cookie("Auth", accessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");

            response.addCookie(accessTokenCookie);

            logger.info("Request ID: {}, Access Token Sent to user: {}, navigating to home page...", requestId, oAuth2Id);

            try {
                response.sendRedirect(frontendUrl);
            } catch (Exception e) {
                logger.warn("Request ID: {}, Fail to navigate user: {} to home page", requestId, oAuth2Id);
                throw new CustomAuthenticationException("Server Error, Please Login Later!");
            }

        } else {
            logger.warn("Request ID: {}, oAuth2Id not found, fail to login", requestId);
            throw new CustomAuthenticationException("Server Error, Please Login Later!");
        }
    }
}
