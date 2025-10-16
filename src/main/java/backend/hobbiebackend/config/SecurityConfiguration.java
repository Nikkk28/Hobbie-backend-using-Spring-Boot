package backend.hobbiebackend.config;

import backend.hobbiebackend.filter.JwtFilter;
import backend.hobbiebackend.security.HobbieUserDetailsService;
import backend.hobbiebackend.security.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {

    private final HobbieUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtFilter jwtFilter;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    public SecurityConfiguration(
            HobbieUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            JwtFilter jwtAuthFilter,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtFilter = jwtAuthFilter;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/authenticate",
                                "/signup",
                                "/register",
                                "/notification",
                                "/password",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/oauth2/**",
                                "/login/oauth2/**"
                        ).permitAll()

                        // Admin only endpoints
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // Business user endpoints
                        .requestMatchers(HttpMethod.POST, "/hobbies").hasRole("BUSINESS_USER")
                        .requestMatchers(HttpMethod.PUT, "/hobbies").hasRole("BUSINESS_USER")
                        .requestMatchers(HttpMethod.DELETE, "/hobbies/**").hasRole("BUSINESS_USER")
                        .requestMatchers("/business/**").hasRole("BUSINESS_USER")

                        // Regular user endpoints
                        .requestMatchers("/client/**", "/test/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/hobbies/save").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/hobbies/remove").hasRole("USER")
                        .requestMatchers("/hobbies/saved").hasRole("USER")

                        // Authenticated users (both USER and BUSINESS_USER)
                        .requestMatchers("/home", "/hobbies/**", "/user/**").authenticated()

                        // All other requests need authentication
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureUrl("/login?error=true")
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}