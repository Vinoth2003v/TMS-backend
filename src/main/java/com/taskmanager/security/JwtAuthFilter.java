package com.taskmanager.security;

import com.taskmanager.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                String role  = jwtUtil.extractRole(token);

                String norm = (role != null ? role.trim().toUpperCase() : "INDIVIDUAL_USER");
                java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + norm));
                if (norm.equals("ADMIN")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else if (norm.equals("MANAGER")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                } else if (norm.equals("TEAM_MEMBER") || norm.equals("TEAM")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_TEAM_MEMBER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_TEAM"));
                } else if (norm.equals("INDIVIDUAL_USER") || norm.equals("USER")) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_INDIVIDUAL_USER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }

                var authToken = new UsernamePasswordAuthenticationToken(email, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ JWT authenticated: email=" + email + ", role=" + norm + ", authorities=" + authorities);
            } else {
                System.out.println("❌ JWT validateToken returned false for token: " + token);
            }
        } catch (Exception e) {
            System.out.println("❌ JWT authentication exception: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}
