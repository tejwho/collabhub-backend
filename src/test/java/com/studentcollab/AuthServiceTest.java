package com.studentcollab;

import com.studentcollab.config.JwtTokenProvider;
import com.studentcollab.dto.AuthResponse;
import com.studentcollab.dto.LoginRequest;
import com.studentcollab.dto.RegisterRequest;
import com.studentcollab.entity.Role;
import com.studentcollab.entity.User;
import com.studentcollab.exception.BadRequestException;
import com.studentcollab.repository.UserRepository;
import com.studentcollab.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john.doe@college.edu");
        request.setPassword("Secret123!");
        request.setFullName("John Doe");
        request.setCollege("Engineering College");
        request.setDepartment("Computer Science");

        when(userRepository.existsByEmail("john.doe@college.edu")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(tokenProvider.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("John Doe", response.getUser().getFullName());
        assertEquals("john.doe@college.edu", response.getUser().getEmail());
        assertEquals(Role.ROLE_STUDENT, response.getUser().getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmailThrowsException() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@college.edu");
        request.setPassword("Secret123!");
        request.setFullName("Existing User");

        when(userRepository.existsByEmail("existing@college.edu")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testLoginSuccess() {
        String rawPassword = "MySecurePassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("alex@college.edu");
        mockUser.setPasswordHash(encodedPassword);
        mockUser.setFullName("Alex Smith");
        mockUser.setRole(Role.ROLE_STUDENT);

        when(userRepository.findByEmail("alex@college.edu")).thenReturn(Optional.of(mockUser));
        when(tokenProvider.generateToken(mockUser)).thenReturn("alex-jwt-token");

        LoginRequest loginRequest = new LoginRequest("alex@college.edu", rawPassword);
        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("alex-jwt-token", response.getToken());
        assertEquals("Alex Smith", response.getUser().getFullName());
    }

    @Test
    void testLoginInvalidPasswordThrowsException() {
        User mockUser = new User();
        mockUser.setEmail("alex@college.edu");
        mockUser.setPasswordHash(passwordEncoder.encode("RealPassword"));

        when(userRepository.findByEmail("alex@college.edu")).thenReturn(Optional.of(mockUser));

        LoginRequest loginRequest = new LoginRequest("alex@college.edu", "WrongPassword");
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
