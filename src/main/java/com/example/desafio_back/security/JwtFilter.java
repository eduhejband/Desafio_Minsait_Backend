package com.example.desafio_back.security;

import com.example.desafio_back.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                String token = auth.substring(7);
                try {
                    if (jwt.isValid(token)) {
                        String login = jwt.getSubject(token);
                        userRepository.findByLogin(login).ifPresent(u -> {
                            var authentication =
                                    new UsernamePasswordAuthenticationToken(login, null, List.of());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
                    }
                } catch (Exception ignored) {
                }
            }
        }
        chain.doFilter(req, res);
    }
}
