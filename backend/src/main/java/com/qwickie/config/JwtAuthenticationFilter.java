package com.qwickie.config;

import com.qwickie.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security Filter that intercepts every incoming HTTP request to validate JWT tokens.
 * This is the core of the stateless authentication architecture. It ensures that 
 * only authenticated users can access protected endpoints like placing an order or 
 * accessing the partner dashboard.
 *
 * @author Ankit Sinha
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    /**
     * The core filter method executed once per HTTP request.
     * It extracts the JWT token from the request, validates its signature and expiration, 
     * and sets the user's authentication context in Spring Security if valid.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        String jwt = null;
        final String username;

        // 1. Try to extract the token from the standard Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        } 
        // 2. Fallback: Try to extract the token from query parameters.
        // This is CRITICAL for Server-Sent Events (EventSource) in the frontend, 
        // because the browser's native EventSource API does not support custom headers.
        else if (request.getParameter("token") != null) {
            jwt = request.getParameter("token");
        }

        // If no token is found, pass the request down the chain (it will be blocked by SecurityConfig if it's a protected endpoint)
        if (jwt == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract the username from the token's payload
            username = jwtService.extractUsername(jwt);
            
            // If we have a username and the current security context isn't already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                // Validate that the token isn't expired and belongs to this user
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create an authentication token and inject it into the Spring Security Context
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Invalid, malformed, or expired token. We swallow the exception and let the 
            // filter chain continue. Spring Security will naturally reject the request 
            // since the SecurityContext remains unauthenticated.
        }

        // Continue processing the request
        filterChain.doFilter(request, response);
    }
}
