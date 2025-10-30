package com.example.bulk_transactions.service;

import com.example.bulk_transactions.dto.LoginRequest;
import com.example.bulk_transactions.dto.RegisterRequest;
import com.example.bulk_transactions.exception.InvalidCredentialsException;
import com.example.bulk_transactions.model.AppUser;
import com.example.bulk_transactions.repository.UserRepository;
import com.example.bulk_transactions.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        userService = new UserService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void registerUserSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("password123");
        request.setRole("ROLE_USER");

        when(userRepository.findByUsername("john")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed");

        userService.registerUser(request);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo("john");
        assertThat(savedUser.getPassword()).isEqualTo("hashed");
        assertThat(savedUser.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    void registerUserUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("john");
        request.setPassword("password123");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new AppUser()));

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already taken");

        verify(userRepository, never()).save(any());
    }

    @Test
    void loginUserSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("password123");

        AppUser user = new AppUser();
        user.setUsername("john");
        user.setPassword("hashed");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        String token = userService.loginUser(request);

        assertThat(token).isEqualTo("jwt-token");
        verify(jwtService, times(1)).generateToken(user);
    }

    @Test
    void loginUserInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("wrong");

        AppUser user = new AppUser();
        user.setUsername("john");
        user.setPassword("hashed");

        when(userRepository.findByUsername("john")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Incorrect credentials");
    }

    @Test
    void loginUserUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loginUser(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Incorrect credentials");
    }
}
