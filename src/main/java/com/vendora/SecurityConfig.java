package com.vendora;
import java.util.List;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer; import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain; import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
@Configuration
public class SecurityConfig {
  @Bean SecurityFilterChain chain(HttpSecurity http, JwtFilter jwt) throws Exception {
    http.csrf(c->c.disable())   // stateless bearer-token API: no cookies or sessions, so no CSRF surface
      .cors(Customizer.withDefaults())
      .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(a->a
        .requestMatchers("/api/auth/register","/api/auth/register-seller","/api/auth/login").permitAll()
        .requestMatchers(HttpMethod.GET,"/api/products/**","/api/categories/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/seller/**").hasRole("SELLER")
        .requestMatchers("/api/**").authenticated()
        .anyRequest().permitAll())
      .exceptionHandling(e->e
        .authenticationEntryPoint((rq,rs,ex)->{rs.setStatus(401);rs.setContentType("application/json");rs.getWriter().write("{\"status\":401,\"message\":\"Authentication required\"}");})
        .accessDeniedHandler((rq,rs,ex)->{rs.setStatus(403);rs.setContentType("application/json");rs.getWriter().write("{\"status\":403,\"message\":\"You do not have access to this resource\"}");}))
      .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
  @Bean PasswordEncoder passwordEncoder(){ return new BCryptPasswordEncoder(); }
  @Bean CorsConfigurationSource corsSource(@Value("${app.cors.origins}") String origins){
    CorsConfiguration c=new CorsConfiguration(); c.setAllowedOrigins(List.of(origins.split(",")));
    c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE")); c.setAllowedHeaders(List.of("Authorization","Content-Type"));
    UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource(); s.registerCorsConfiguration("/api/**",c); return s; }
}
