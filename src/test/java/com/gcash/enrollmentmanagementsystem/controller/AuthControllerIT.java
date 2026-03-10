package com.gcash.enrollmentmanagementsystem.controller;

import com.gcash.enrollmentmanagementsystem.AbstractIntegrationTest;
import com.gcash.enrollmentmanagementsystem.dto.auth.LoginRequest;
import com.gcash.enrollmentmanagementsystem.dto.auth.RefreshTokenRequest;
import com.gcash.enrollmentmanagementsystem.dto.auth.RegisterRequest;
import com.gcash.enrollmentmanagementsystem.dto.auth.TokenResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerIT extends AbstractIntegrationTest {

    @Nested
    @DisplayName("Login Flow Integration Tests")
    class LoginFlowTests {

        @Test
        @DisplayName("Should login successfully with seeded admin user")
        void testLoginFlow_AdminUser_Success() throws Exception {
            // Given - admin user from V2__seed_data.sql
            LoginRequest request = LoginRequest.builder()
                    .username("admin1")
                    .password("admin123")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").isNotEmpty())
                    .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                    .andExpect(jsonPath("$.token_type").value("Bearer"))
                    .andExpect(jsonPath("$.expires_in").isNumber());
        }

        @Test
        @DisplayName("Should login successfully with seeded student user")
        void testLoginFlow_StudentUser_Success() throws Exception {
            // Given - student user from V2__seed_data.sql
            LoginRequest request = LoginRequest.builder()
                    .username("jdoe")
                    .password("student123")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").isNotEmpty())
                    .andExpect(jsonPath("$.refresh_token").isNotEmpty());
        }

        @ParameterizedTest(name = "Login with username=''{0}'' and password=''{1}'' should fail")
        @CsvSource({
                "wronguser, admin123",
                "admin1, wrongpassword",
                "nonexistent, password123"
        })
        @DisplayName("Should return 401 for invalid credentials")
        void testLoginFlow_InvalidCredentials_Returns401(String username, String password) throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username(username)
                    .password(password)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for missing username")
        void testLoginFlow_MissingUsername_Returns400() throws Exception {
            // Given
            String invalidRequest = "{\"password\": \"password123\"}";

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Register and Login End-to-End Tests")
    class RegisterAndLoginTests {

        @Test
        @DisplayName("Should register new user and login successfully")
        void testRegisterAndLogin_EndToEnd() throws Exception {
            // Given - unique username for this test
            String uniqueUsername = "newuser_" + UUID.randomUUID().toString().substring(0, 8);
            String email = uniqueUsername + "@example.com";

            RegisterRequest registerRequest = RegisterRequest.builder()
                    .username(uniqueUsername)
                    .email(email)
                    .password("password123")
                    .firstName("New")
                    .lastName("User")
                    .dateOfBirth(LocalDate.of(2000, 5, 15))
                    .degreeId(1L)
                    .build();

            // Step 1: Register new user
            MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.access_token").isNotEmpty())
                    .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                    .andReturn();

            TokenResponse registerResponse = objectMapper.readValue(
                    registerResult.getResponse().getContentAsString(),
                    TokenResponse.class
            );

            assertThat(registerResponse.getAccessToken()).isNotBlank();
            assertThat(registerResponse.getRefreshToken()).isNotBlank();

            // Step 2: Login with newly registered user
            LoginRequest loginRequest = LoginRequest.builder()
                    .username(uniqueUsername)
                    .password("password123")
                    .build();

            MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").isNotEmpty())
                    .andReturn();

            TokenResponse loginResponse = objectMapper.readValue(
                    loginResult.getResponse().getContentAsString(),
                    TokenResponse.class
            );

            assertThat(loginResponse.getAccessToken()).isNotBlank();
        }

        @Test
        @DisplayName("Should fail registration with duplicate username")
        void testRegister_DuplicateUsername_Returns400() throws Exception {
            // Given - admin user already exists from seed data
            RegisterRequest request = RegisterRequest.builder()
                    .username("admin1")
                    .email("newemail@example.com")
                    .password("password123")
                    .firstName("Test")
                    .lastName("User")
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .degreeId(1L)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Username")));
        }

        @Test
        @DisplayName("Should fail registration with duplicate email")
        void testRegister_DuplicateEmail_Returns400() throws Exception {
            // Given - use email from seed data
            RegisterRequest request = RegisterRequest.builder()
                    .username("uniqueuser_" + UUID.randomUUID().toString().substring(0, 8))
                    .email("admin1@obu.edu")
                    .password("password123")
                    .firstName("Test")
                    .lastName("User")
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .degreeId(1L)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(containsString("Email")));
        }

        @ParameterizedTest(name = "Registration validation: {0}")
        @CsvSource({
                "Short username, ab, test@test.com, password123",
                "Invalid email, validuser, notanemail, password123",
                "Short password, validuser, test@test.com, 12345"
        })
        @DisplayName("Should validate registration input")
        void testRegister_ValidationErrors(String scenario, String username, String email, String password) throws Exception {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username(username)
                    .email(email)
                    .password(password)
                    .firstName("Test")
                    .lastName("User")
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .degreeId(1L)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh access token with valid refresh token")
        void testRefreshToken_Success() throws Exception {
            // Given - Login first to get tokens
            TokenResponse loginResponse = loginAndGetTokenResponse("admin1", "admin123");

            RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                    .refreshToken(loginResponse.getRefreshToken())
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(refreshRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").isNotEmpty())
                    .andExpect(jsonPath("$.refresh_token").isNotEmpty());
        }

        @Test
        @DisplayName("Should fail refresh with invalid token")
        void testRefreshToken_InvalidToken_Returns401() throws Exception {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalid.refresh.token")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully with valid token")
        void testLogout_Success() throws Exception {
            // Given
            String token = loginAndGetToken("admin1", "admin123");

            // When & Then
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }

        @Test
        @DisplayName("Should handle logout without token gracefully")
        void testLogout_WithoutToken() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Logged out successfully"));
        }
    }
}
