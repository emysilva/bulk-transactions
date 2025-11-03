package com.example.bulk_transactions.controller;

import com.example.bulk_transactions.dto.auth.LoginRequest;
import com.example.bulk_transactions.dto.auth.RegisterRequest;
import com.example.bulk_transactions.exception.BadRequestException;
import com.example.bulk_transactions.exception.InvalidCredentialsException;
import com.example.bulk_transactions.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("john");
        registerRequest.setPassword("Password@123");
        registerRequest.setRole("ROLE_USER");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("john");
        loginRequest.setPassword("Password@123");
    }

    @Test
    void registerUserSuccess() throws Exception {
        doNothing().when(userService).registerUser(any(RegisterRequest.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User created successfully"));

        verify(userService, times(1)).registerUser(any(RegisterRequest.class));
    }

    @Test
    void registerUserExistingUser() throws Exception {
        doThrow(new BadRequestException("Username already taken"))
                .when(userService).registerUser(any(RegisterRequest.class));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginUserSuccess() throws Exception {
        when(userService.loginUser(any(LoginRequest.class))).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    void loginUserInvalidCredentials() throws Exception {
        when(userService.loginUser(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Incorrect credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
