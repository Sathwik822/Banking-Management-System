package com.bank.service;

import com.bank.dto.Dto.*;
import com.bank.entity.User;
import com.bank.exception.BankException;
import com.bank.repository.UserRepository;
import com.bank.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.users = users; this.encoder = encoder; this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new BankException("Email is already registered", HttpStatus.CONFLICT);
        }
        User u = users.save(new User(req.fullName().trim(), email, encoder.encode(req.password()), "CUSTOMER"));
        return new AuthResponse(jwtUtil.generateToken(u.getEmail(), u.getRole()), u.getFullName(), u.getEmail(), u.getRole());
    }

    public AuthResponse login(LoginRequest req) {
        User u = users.findByEmail(req.email().trim().toLowerCase())
                .filter(x -> encoder.matches(req.password(), x.getPassword()))
                .orElseThrow(() -> new BankException("Invalid email or password", HttpStatus.UNAUTHORIZED));
        return new AuthResponse(jwtUtil.generateToken(u.getEmail(), u.getRole()), u.getFullName(), u.getEmail(), u.getRole());
    }
}
