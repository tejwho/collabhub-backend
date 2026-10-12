package com.studentcollab.service;

import com.studentcollab.config.JwtTokenProvider;
import com.studentcollab.dto.AuthResponse;
import com.studentcollab.dto.LoginRequest;
import com.studentcollab.dto.RegisterRequest;
import com.studentcollab.dto.UserProfileResponse;
import com.studentcollab.entity.Role;
import com.studentcollab.entity.User;
import com.studentcollab.exception.BadRequestException;
import com.studentcollab.exception.ResourceNotFoundException;
import com.studentcollab.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName().trim());
        user.setCollege(request.getCollege());
        user.setDepartment(request.getDepartment());
        user.setRollNumber(request.getRollNumber());
        user.setYearOfStudy(request.getYearOfStudy());
        user.setGraduationYear(request.getGraduationYear());
        user.setBio(request.getBio());
        user.setGithubUrl(request.getGithubUrl());
        user.setLinkedinUrl(request.getLinkedinUrl());
        user.setPortfolioUrl(request.getPortfolioUrl());
        user.setRole(Role.ROLE_STUDENT);

        User savedUser = userRepository.save(user);
        String token = tokenProvider.generateToken(savedUser);

        return new AuthResponse(token, UserProfileResponse.fromEntity(savedUser));
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = tokenProvider.generateToken(user);
        return new AuthResponse(token, UserProfileResponse.fromEntity(user));
    }

    public UserProfileResponse getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return UserProfileResponse.fromEntity(user);
    }
}
