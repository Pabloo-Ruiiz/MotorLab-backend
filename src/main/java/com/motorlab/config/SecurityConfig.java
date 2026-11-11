package com.motorlab.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.motorlab.security.JwtAuthenticationFilter;
import com.motorlab.security.LimiteIntentosLoginFilter;
import com.motorlab.security.RestAccessDeniedHandler;
import com.motorlab.security.RestAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter filtroJwt;
    private final LimiteIntentosLoginFilter filtroLimiteIntentosLogin;
    private final RestAuthenticationEntryPoint puntoEntrada;
    private final RestAccessDeniedHandler manejadorAccesoDenegado;

    public SecurityConfig(
            JwtAuthenticationFilter filtroJwt,
            LimiteIntentosLoginFilter filtroLimiteIntentosLogin,
            RestAuthenticationEntryPoint puntoEntrada,
            RestAccessDeniedHandler manejadorAccesoDenegado) {
        this.filtroJwt = filtroJwt;
        this.filtroLimiteIntentosLogin = filtroLimiteIntentosLogin;
        this.puntoEntrada = puntoEntrada;
        this.manejadorAccesoDenegado = manejadorAccesoDenegado;
    }

    @Bean
    public SecurityFilterChain cadenaDeSeguridad(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(configuracionCors()))
                .sessionManagement(sesiones -> sesiones.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(excepciones -> excepciones
                        .authenticationEntryPoint(puntoEntrada)
                        .accessDeniedHandler(manejadorAccesoDenegado))
                .authorizeHttpRequests(autorizacion -> autorizacion
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/auth/registro",
                                "/api/v1/auth/login",
                                "/api/v1/auth/verificar-email",
                                "/api/v1/auth/reenviar-verificacion",
                                // Lo llama Stripe: se autentica con la firma del aviso
                                "/api/v1/pagos/stripe/webhook").permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        // Frontend Angular: archivos estáticos y rutas que resuelve el navegador
                        .requestMatchers(
                                HttpMethod.GET,
                                "/",
                                "/index.html",
                                "/*.js",
                                "/*.css",
                                "/*.ico",
                                "/*.svg",
                                "/assets/**",
                                "/media/**",
                                "/cliente/**",
                                "/taller/**",
                                "/login",
                                "/registro",
                                "/verificar-correo",
                                "/403",
                                "/404").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(filtroLimiteIntentosLogin, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource configuracionCors() {
        CorsConfiguration configuracion = new CorsConfiguration();
        configuracion.setAllowedOrigins(List.of("http://localhost:4200"));
        configuracion.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuracion.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource origenes = new UrlBasedCorsConfigurationSource();
        origenes.registerCorsConfiguration("/**", configuracion);
        return origenes;
    }

    @Bean
    public PasswordEncoder codificadorPassword() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager gestorAutenticacion(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }
}
