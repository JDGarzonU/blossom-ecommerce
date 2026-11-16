
package com.blossom.ecommerce.controller;

import com.blossom.ecommerce.dto.AuthResponse;
import com.blossom.ecommerce.exception.GlobalExceptionHandler;
import com.blossom.ecommerce.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private AuthResponse customerResponse() {
        return new AuthResponse(
                "test-jwt-token",
                "Bearer",
                1L,
                "Cliente Prueba",
                "cliente@blossom.test",
                "CUSTOMER"
        );
    }

    @Test
    void shouldRegisterCustomer() throws Exception {
        when(authService.register(any()))
                .thenReturn(customerResponse());

        String json = """
                {
                  "name": "Cliente Prueba",
                  "email": "cliente@blossom.test",
                  "password": "Blossom123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accessToken")
                        .value("test-jwt-token"));
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        when(authService.login(any()))
                .thenReturn(customerResponse());

        String json = """
                {
                  "email": "cliente@blossom.test",
                  "password": "Blossom123!"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void shouldRejectDuplicateRegistration() throws Exception {
        when(authService.register(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email already registered"
                ));

        String json = """
                {
                  "name": "Cliente Prueba",
                  "email": "cliente@blossom.test",
                  "password": "Blossom123!"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void shouldRejectInvalidLogin() throws Exception {
        when(authService.login(any()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid credentials"
                ));

        String json = """
                {
                  "email": "cliente@blossom.test",
                  "password": "WrongPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldRejectInvalidRegistrationBody() throws Exception {
        String json = """
                {
                  "name": "",
                  "email": "invalid-email",
                  "password": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void shouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{invalid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(authService);
    }
}
