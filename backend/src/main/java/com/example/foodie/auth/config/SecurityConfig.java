package com.example.foodie.auth.config;

import com.example.foodie.auth.exceptionhandler.CustomAccessDeniedHandler;
import com.example.foodie.auth.exceptionhandler.CustomAuthenticationEntryPoint;
import com.example.foodie.auth.security.JWTFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${api.prefix}")
    private String apiPrefix;

    @Value("${frontend.url}")
    private String frontendURL;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final DaoAuthenticationProvider provider;
    private final JWTFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{

        return http
                .csrf(customizer -> customizer.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                            .permitAll()

                        .requestMatchers(GET, String.format("%s/tags", apiPrefix))
                            .permitAll()

                        .requestMatchers(POST,
                                String.format("%s/users/login", apiPrefix),
                                String.format("%s/users/register", apiPrefix),
                                String.format("%s/users/refresh", apiPrefix),
                                String.format("%s/users/logout", apiPrefix))
                            .permitAll()

                        .requestMatchers(GET,
                                String.format("%s/dishes", apiPrefix),
                                String.format("%s/dishes/average_rating", apiPrefix),
                                String.format("%s/dishes/allIds", apiPrefix),
                                String.format("%s/reviews/dish/**", apiPrefix))
                            .permitAll()

                        .requestMatchers(String.format("%s/admin/**", apiPrefix))
                            .hasRole("ADMIN")

                        .requestMatchers(POST, String.format("%s/images", apiPrefix))
                            .hasRole("ADMIN")

                        .requestMatchers(POST, String.format("%s/dish-tag/**", apiPrefix))
                            .hasRole("ADMIN")

                        .anyRequest()
                            .authenticated()
                )

                .authenticationProvider(provider)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendURL));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
    

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(   );
        source.registerCorsConfiguration("/**", config);
        return source;
    }





}
