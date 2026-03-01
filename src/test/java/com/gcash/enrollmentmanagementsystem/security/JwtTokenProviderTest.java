package com.gcash.enrollmentmanagementsystem.security;

import com.gcash.enrollmentmanagementsystem.entity.User;
import com.gcash.enrollmentmanagementsystem.enums.Role;
import com.gcash.enrollmentmanagementsystem.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "dGVzdHNlY3JldGtleWZvcmp3dHRva2VuZ2VuZXJhdGlvbmFuZGl0c2hvdWxkYmVhdGxlYXN0MjU2Yml0c2xvbmc=";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 900000L); // 15 minutes
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 604800000L); // 7 days
    }

    @Nested
    @DisplayName("Generate Token Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate valid access token with correct claims")
        void testGenerateToken_ValidClaims() {
            // Given
            User user = TestDataBuilder.aUser()
                    .id(1L)
                    .username("testuser")
                    .email("test@example.com")
                    .role(Role.STUDENT)
                    .build();
            UserPrincipal userPrincipal = UserPrincipal.create(user);

            // When
            String token = jwtTokenProvider.generateAccessToken(userPrincipal);

            // Then
            assertThat(token).isNotNull().isNotEmpty();
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
            assertThat(jwtTokenProvider.getUsernameFromToken(token)).isEqualTo("testuser");
            assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should generate valid refresh token")
        void testGenerateRefreshToken_ValidClaims() {
            // Given
            User user = TestDataBuilder.aUser()
                    .id(1L)
                    .username("testuser")
                    .build();
            UserPrincipal userPrincipal = UserPrincipal.create(user);

            // When
            String token = jwtTokenProvider.generateRefreshToken(userPrincipal);

            // Then
            assertThat(token).isNotNull().isNotEmpty();
            assertThat(jwtTokenProvider.validateToken(token)).isTrue();
            assertThat(jwtTokenProvider.isRefreshToken(token)).isTrue();
        }

        @Test
        @DisplayName("Access token should not be identified as refresh token")
        void testAccessToken_IsNotRefreshToken() {
            // Given
            User user = TestDataBuilder.aUser().id(1L).build();
            UserPrincipal userPrincipal = UserPrincipal.create(user);

            // When
            String accessToken = jwtTokenProvider.generateAccessToken(userPrincipal);

            // Then
            assertThat(jwtTokenProvider.isRefreshToken(accessToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("Validate Token Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should return true for valid token")
        void testValidateToken_Valid() {
            // Given
            User user = TestDataBuilder.aUser().id(1L).build();
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String token = jwtTokenProvider.generateAccessToken(userPrincipal);

            // When
            boolean isValid = jwtTokenProvider.validateToken(token);

            // Then
            assertThat(isValid).isTrue();
        }

        @ParameterizedTest(name = "Token expiration={0}ms should result in expired token")
        @ValueSource(longs = {1L, 10L, 100L})
        @DisplayName("Should return false for expired token")
        void testValidateToken_Expired(long expirationMs) throws InterruptedException {
            // Given
            JwtTokenProvider shortLivedProvider = new JwtTokenProvider();
            ReflectionTestUtils.setField(shortLivedProvider, "jwtSecret", TEST_SECRET);
            ReflectionTestUtils.setField(shortLivedProvider, "accessTokenExpiration", expirationMs);
            ReflectionTestUtils.setField(shortLivedProvider, "refreshTokenExpiration", expirationMs);

            User user = TestDataBuilder.aUser().id(1L).build();
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String token = shortLivedProvider.generateAccessToken(userPrincipal);

            // Wait for token to expire
            Thread.sleep(expirationMs + 100);

            // When
            boolean isValid = shortLivedProvider.validateToken(token);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for malformed token")
        void testValidateToken_Malformed() {
            // Given
            String malformedToken = "not.a.valid.jwt.token";

            // When
            boolean isValid = jwtTokenProvider.validateToken(malformedToken);

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty token")
        void testValidateToken_Empty() {
            // When
            boolean isValid = jwtTokenProvider.validateToken("");

            // Then
            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for token with wrong signature")
        void testValidateToken_WrongSignature() {
            // Given - Create token with different secret
            JwtTokenProvider otherProvider = new JwtTokenProvider();
            ReflectionTestUtils.setField(otherProvider, "jwtSecret",
                    "YW5vdGhlcnNlY3JldGtleWZvcmp3dHRva2VuZ2VuZXJhdGlvbmFuZGl0c2hvdWxkYmVhdGxlYXN0MjU2Yml0cw==");
            ReflectionTestUtils.setField(otherProvider, "accessTokenExpiration", 900000L);
            ReflectionTestUtils.setField(otherProvider, "refreshTokenExpiration", 604800000L);

            User user = TestDataBuilder.aUser().id(1L).build();
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String tokenWithDifferentSecret = otherProvider.generateAccessToken(userPrincipal);

            // When - Validate with original provider
            boolean isValid = jwtTokenProvider.validateToken(tokenWithDifferentSecret);

            // Then
            assertThat(isValid).isFalse();
        }
    }

    @Nested
    @DisplayName("Extract Claims Tests")
    class ExtractClaimsTests {

        @Test
        @DisplayName("Should extract username from token")
        void testGetUsernameFromToken() {
            // Given
            User user = TestDataBuilder.aUser()
                    .id(1L)
                    .username("johndoe")
                    .build();
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String token = jwtTokenProvider.generateAccessToken(userPrincipal);

            // When
            String username = jwtTokenProvider.getUsernameFromToken(token);

            // Then
            assertThat(username).isEqualTo("johndoe");
        }

        @Test
        @DisplayName("Should extract user ID from token")
        void testGetUserIdFromToken() {
            // Given
            User user = TestDataBuilder.aUser()
                    .id(42L)
                    .build();
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            String token = jwtTokenProvider.generateAccessToken(userPrincipal);

            // When
            Long userId = jwtTokenProvider.getUserIdFromToken(token);

            // Then
            assertThat(userId).isEqualTo(42L);
        }
    }
}
