package com.gcash.enrollmentmanagementsystem.service;

import com.gcash.enrollmentmanagementsystem.dto.auth.LoginRequest;
import com.gcash.enrollmentmanagementsystem.dto.auth.RegisterRequest;
import com.gcash.enrollmentmanagementsystem.dto.auth.TokenResponse;
import com.gcash.enrollmentmanagementsystem.entity.Degree;
import com.gcash.enrollmentmanagementsystem.entity.Student;
import com.gcash.enrollmentmanagementsystem.entity.User;
import com.gcash.enrollmentmanagementsystem.enums.Role;
import com.gcash.enrollmentmanagementsystem.exception.BadRequestException;
import com.gcash.enrollmentmanagementsystem.repository.DegreeRepository;
import com.gcash.enrollmentmanagementsystem.repository.StudentRepository;
import com.gcash.enrollmentmanagementsystem.repository.UserRepository;
import com.gcash.enrollmentmanagementsystem.security.JwtTokenProvider;
import com.gcash.enrollmentmanagementsystem.security.UserPrincipal;
import com.gcash.enrollmentmanagementsystem.util.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private DegreeRepository degreeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void testLogin_Success() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username("testuser")
                    .password("password123")
                    .build();

            User user = TestDataBuilder.aUser().id(1L).build();
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal, null, userPrincipal.getAuthorities());

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(authentication)).thenReturn("refresh-token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900L);

            // When
            TokenResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getExpiresIn()).isEqualTo(900L);
        }

        @ParameterizedTest(name = "Login with username=''{0}'' and password=''{1}'' should fail")
        @CsvSource({
                "wronguser, password123",
                "testuser, wrongpassword",
                "wronguser, wrongpassword",
                "'', password123",
                "testuser, ''"
        })
        @DisplayName("Should throw BadCredentialsException for invalid credentials")
        void testLogin_InvalidCredentials(String username, String password) {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .username(username)
                    .password(password)
                    .build();

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new student successfully")
        void testRegister_Success() {
            // Given
            Degree degree = TestDataBuilder.aDegree().id(1L).build();
            RegisterRequest request = RegisterRequest.builder()
                    .username("newuser")
                    .email("newuser@example.com")
                    .password("password123")
                    .firstName("John")
                    .lastName("Doe")
                    .dateOfBirth(LocalDate.of(2000, 1, 15))
                    .degreeId(1L)
                    .build();

            User savedUser = TestDataBuilder.aUser()
                    .id(1L)
                    .username("newuser")
                    .email("newuser@example.com")
                    .build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(degreeRepository.findById(1L)).thenReturn(Optional.of(degree));
            when(studentRepository.count()).thenReturn(0L);
            when(studentRepository.save(any(Student.class))).thenReturn(
                    TestDataBuilder.aStudent().id(1L).user(savedUser).degree(degree).build());
            when(jwtTokenProvider.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
            when(jwtTokenProvider.generateRefreshToken(any(UserPrincipal.class))).thenReturn("refresh-token");
            when(jwtTokenProvider.getAccessTokenExpiration()).thenReturn(900L);

            // When
            TokenResponse response = authService.register(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

            verify(userRepository).save(argThat(user ->
                    user.getUsername().equals("newuser") &&
                            user.getRole() == Role.STUDENT
            ));
            verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException for duplicate username")
        void testRegister_DuplicateUsername_ThrowsException() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("existinguser")
                    .email("new@example.com")
                    .password("password123")
                    .firstName("John")
                    .lastName("Doe")
                    .degreeId(1L)
                    .build();

            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Username is already taken");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException for duplicate email")
        void testRegister_DuplicateEmail_ThrowsException() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("newuser")
                    .email("existing@example.com")
                    .password("password123")
                    .firstName("John")
                    .lastName("Doe")
                    .degreeId(1L)
                    .build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email is already registered");

            verify(userRepository, never()).save(any(User.class));
        }
    }
}
