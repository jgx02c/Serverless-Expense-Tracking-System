package com.expensetracker.config;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    @Value("${aws.region}")
    private String region;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            .antMatchers("/api/expenses/**").authenticated()
            .anyRequest().permitAll()
            .and()
            .addFilterBefore(cognitoAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OncePerRequestFilter cognitoAuthenticationFilter() {
        return new OncePerRequestFilter() {
            private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    try {
                        JWTClaimsSet claims = getJwtProcessor().process(token, null);
                        String userId = claims.getSubject();
                        request.setAttribute("userId", userId);
                    } catch (Exception e) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                }
                filterChain.doFilter(request, response);
            }

            private ConfigurableJWTProcessor<SecurityContext> getJwtProcessor() {
                if (jwtProcessor == null) {
                    try {
                        String issuer = String.format("https://cognito-idp.%s.amazonaws.com/%s", region, userPoolId);
                        URL jwkSetURL = new URL(issuer + "/.well-known/jwks.json");
                        JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(jwkSetURL);
                        JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(
                            JWSAlgorithm.RS256, keySource);
                        jwtProcessor = new DefaultJWTProcessor<>();
                        jwtProcessor.setJWSKeySelector(keySelector);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to initialize JWT processor", e);
                    }
                }
                return jwtProcessor;
            }
        };
    }
} 