
package com.blossom.ecommerce.exception;

import com.blossom.ecommerce.dto.RegisterRequest;
import jakarta.validation.Valid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @RestController
    @RequestMapping("/test/errors")
    static class ErrorTestController {

        @GetMapping("/not-found")
        public String notFound() {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Resource not found"
            );
        }

        @GetMapping("/conflict")
        public String conflict() {
            throw new DataIntegrityViolationException(
                    "Internal database constraint information"
            );
        }

        @GetMapping("/unexpected")
        public String unexpected() {
            throw new IllegalStateException(
                    "Internal technical details must not be exposed"
            );
        }

        @PostMapping("/validation")
        public String validation(
                @Valid @RequestBody RegisterRequest request
        ) {
            return "OK";
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldHandleNotFound() throws Exception {
        mockMvc.perform(get("/test/errors/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Resource not found"))
                .andExpect(jsonPath("$.path")
                        .value("/test/errors/not-found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldHandleDatabaseConflict() throws Exception {
        mockMvc.perform(get("/test/errors/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "The operation conflicts with existing data"
                ))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void shouldHideInternalServerErrors() throws Exception {
        mockMvc.perform(get("/test/errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(
                        "An unexpected server error occurred"
                ))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "email": "incorrecto",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void shouldHandleMalformedJson() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "Invalid or missing JSON request body"
                ));
    }

    @Test
    void shouldHandleMissingRequestBody() throws Exception {
        mockMvc.perform(post("/test/errors/validation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
