package com.escuelaposgrado.Intranet.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.escuelaposgrado.Intranet.security.jwt.AuthEntryPointJwt;
import com.escuelaposgrado.Intranet.security.jwt.AuthTokenFilter;
import com.escuelaposgrado.Intranet.security.services.UserDetailsServiceImpl;

/**
 * Configuración de seguridad para el microservicio de Intranet.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /**
     * Configuración principal de la cadena de seguridad.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        configureCors(http);
        configureCsrf(http);
        configureExceptionHandling(http);
        configureSession(http);
        configureAuthentication(http);
        configureAuthorization(http);
        configureJwtFilter(http);

        return http.build();
    }

    /**
     * Configura CORS.
     */
    private void configureCors(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(
                corsConfigurationSource()));
    }

    /**
     * Configura CSRF.
     */
    private void configureCsrf(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
    }

    /**
     * Configura el tratamiento de autenticación no autorizada.
     */
    private void configureExceptionHandling(HttpSecurity http)
            throws Exception {

        http.exceptionHandling(exception -> exception.authenticationEntryPoint(
                unauthorizedHandler));
    }

    /**
     * Configura la sesión como stateless.
     */
    private void configureSession(HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS));
    }

    /**
     * Configura el proveedor de autenticación.
     */
    private void configureAuthentication(HttpSecurity http)
            throws Exception {

        http.authenticationProvider(
                authenticationProvider());
    }

    /**
     * Configura las reglas de autorización.
     */
    private void configureAuthorization(HttpSecurity http)
            throws Exception {

        http.authorizeHttpRequests(authz -> authz
                // Endpoints públicos
                .requestMatchers("/").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/health/**").permitAll()

                // Permitir peticiones OPTIONS para CORS
                .requestMatchers(HttpMethod.OPTIONS).permitAll()

                // Todos los demás endpoints requieren autenticación
                .anyRequest().authenticated());
    }

    /**
     * Agrega el filtro JWT antes del filtro de autenticación estándar.
     */
    private void configureJwtFilter(HttpSecurity http)
            throws Exception {

        http.addFilterBefore(
                authenticationJwtTokenFilter(),
                UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * Crea el filtro JWT.
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Configura el proveedor de autenticación.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(
                userDetailsService);

        authProvider.setPasswordEncoder(
                passwordEncoder());

        return authProvider;
    }

    /**
     * Crea el AuthenticationManager.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig)
            throws Exception {

        return authConfig.getAuthenticationManager();
    }

    /**
     * Configura el encoder de contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura CORS para el microservicio.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                Arrays.asList(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000"));

        configuration.addAllowedMethod("*");

        configuration.setAllowedHeaders(
                Arrays.asList("*"));

        configuration.setAllowCredentials(true);

        configuration.setExposedHeaders(
                Arrays.asList(
                        "Authorization",
                        "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration);

        return source;
    }
}