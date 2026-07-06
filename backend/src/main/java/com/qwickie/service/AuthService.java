package com.qwickie.service;

import com.qwickie.config.JwtService;
import com.qwickie.dto.AuthRequest;
import com.qwickie.dto.AuthResponse;
import com.qwickie.dto.RegisterRequest;
import com.qwickie.model.User;
import com.qwickie.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
/**
 * @author Ankit Sinha
 */
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterRequest request) {
        User user = new User(
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                User.Role.valueOf(request.getRole()),
                request.getPhone()
        );
        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, user.getRole().name());
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, user.getRole().name());
    }
}
