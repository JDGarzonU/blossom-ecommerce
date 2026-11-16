
package com.blossom.ecommerce.service;

import com.blossom.ecommerce.dto.AuthResponse;
import com.blossom.ecommerce.dto.LoginRequest;
import com.blossom.ecommerce.dto.RegisterRequest;
import com.blossom.ecommerce.entity.Role;
import com.blossom.ecommerce.entity.User;
import com.blossom.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                authService,
                "expirationMinutes",
                120L
        );
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Cliente Prueba");
        user.setEmail("cliente@blossom.test");
        user.setPassword("hashed-password");
        user.setRole(Role.CUSTOMER);
        return user;
    }

    private void mockJwtGeneration() {
        Jwt jwt = mock(Jwt.class);

        when(jwt.getTokenValue())
                .thenReturn("test-jwt-token");

        when(jwtEncoder.encode(
                any(JwtEncoderParameters.class)
        )).thenReturn(jwt);
    }

    @Test
    void shouldRegisterCustomer() {
        RegisterRequest request = new RegisterRequest(
                "Cliente Prueba",
                "cliente@blossom.test",
                "Blossom123!"
        );

        when(userRepository.existsByEmailIgnoreCase(
                "cliente@blossom.test"
        )).thenReturn(false);

        when(passwordEncoder.encode("Blossom123!"))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        mockJwtGeneration();

        AuthResponse response = authService.register(request);

        assertEquals(1L, response.userId());
        assertEquals("CUSTOMER", response.role());
        assertEquals("Bearer", response.tokenType());
        assertEquals("test-jwt-token", response.accessToken());

        verify(passwordEncoder).encode("Blossom123!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldNormalizeEmailOnRegistration() {
        RegisterRequest request = new RegisterRequest(
                "Cliente Prueba",
                "  CLIENTE@BLOSSOM.TEST  ",
                "Blossom123!"
        );

        when(userRepository.existsByEmailIgnoreCase(
                "cliente@blossom.test"
        )).thenReturn(false);

        when(passwordEncoder.encode(anyString()))
                .thenReturn("hashed-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });

        mockJwtGeneration();

        AuthResponse response = authService.register(request);

        assertEquals(
                "cliente@blossom.test",
                response.email()
        );
    }

    @Test
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "Cliente Prueba",
                "cliente@blossom.test",
                "Blossom123!"
        );

        when(userRepository.existsByEmailIgnoreCase(
                "cliente@blossom.test"
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.register(request)
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, jwtEncoder);
    }

    @Test
    void shouldLoginSuccessfully() {
        User user = createUser();

        when(userRepository.findByEmailIgnoreCase(
                "cliente@blossom.test"
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Blossom123!",
                "hashed-password"
        )).thenReturn(true);

        mockJwtGeneration();

        AuthResponse response = authService.login(
                new LoginRequest(
                        "cliente@blossom.test",
                        "Blossom123!"
                )
        );

        assertEquals("CUSTOMER", response.role());
        assertEquals(1L, response.userId());
        assertEquals(
                "test-jwt-token",
                response.accessToken()
        );
    }

    @Test
    void shouldRejectUnknownUser() {
        when(userRepository.findByEmailIgnoreCase(
                "unknown@blossom.test"
        )).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(
                        new LoginRequest(
                                "unknown@blossom.test",
                                "Incorrect123!"
                        )
                )
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(passwordEncoder, jwtEncoder);
    }

    @Test
    void shouldRejectIncorrectPassword() {
        User user = createUser();

        when(userRepository.findByEmailIgnoreCase(
                "cliente@blossom.test"
        )).thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "hashed-password"
        )).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(
                        new LoginRequest(
                                "cliente@blossom.test",
                                "WrongPassword"
                        )
                )
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verifyNoInteractions(jwtEncoder);
    }
}
