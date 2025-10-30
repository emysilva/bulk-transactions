package com.example.bulk_transactions.controller;

import com.example.bulk_transactions.dto.BulkTransactionRequest;
import com.example.bulk_transactions.dto.TransactionServiceRequest;
import com.example.bulk_transactions.model.AppUser;
import com.example.bulk_transactions.repository.UserRepository;
import com.example.bulk_transactions.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class BulkTransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String userValidToken;

    private String adminValidToken;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        AppUser user = new AppUser();
        user.setUsername("user");
        user.setPassword(passwordEncoder.encode("Password@123"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        userValidToken = jwtService.generateToken(user);

        AppUser admin = new AppUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("Password@123"));
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);

        adminValidToken = jwtService.generateToken(admin);
    }


    @Test
    void testProcessBulkTransactionsSuccess() throws Exception {
        BulkTransactionRequest request = new BulkTransactionRequest(
                "batch-1",
                List.of(
                        new TransactionServiceRequest("tx-1", "src1", "dest1", BigDecimal.valueOf(1000)),
                        new TransactionServiceRequest("tx-2", "src2", "dest2", BigDecimal.valueOf(2000))
                )
        );

        mockMvc.perform(post("/api/v1/bulk-transactions")
                        .header("Authorization", "Bearer " + userValidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testProcessBulkTransactionsUnauthorized() throws Exception {
        BulkTransactionRequest request = new BulkTransactionRequest(
                "batch-1",
                List.of(
                        new TransactionServiceRequest("tx-2", "src2", "dest2", BigDecimal.valueOf(2000))
                )
        );

        mockMvc.perform(post("/api/v1/bulk-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAdminCanAccessHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + adminValidToken))
                .andExpect(status().isOk());
    }

    @Test
    void testUserCannotAccessHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health")
                        .header("Authorization", "Bearer " + userValidToken))
                .andExpect(status().isForbidden());
    }

}
