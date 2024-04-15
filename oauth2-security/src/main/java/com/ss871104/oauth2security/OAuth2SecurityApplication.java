package com.ss871104.oauth2security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.ss871104.oauth2security.util.Constant.WHITE_LIST_URL;

@SpringBootApplication
public class OAuth2SecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(OAuth2SecurityApplication.class, args);
	}

	@Bean
	public OrRequestMatcher whiteListMatcher() {
		List<RequestMatcher> whiteListMatchers = Arrays.stream(WHITE_LIST_URL)
				.map(AntPathRequestMatcher::new)
				.collect(Collectors.toList());
		return new OrRequestMatcher(whiteListMatchers);
	}

}
