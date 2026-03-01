package com.gcash.enrollmentmanagementsystem.controller;

import com.gcash.enrollmentmanagementsystem.AbstractIntegrationTest;
import com.gcash.enrollmentmanagementsystem.dto.*;
import com.gcash.enrollmentmanagementsystem.repository.SectionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminControllerIT extends AbstractIntegrationTest {

    @Autowired
    private SectionRepository sectionRepository;

    @Nested
    @DisplayName("Admin Student Management Tests")
    class StudentManagementTests {

        @Test
        @DisplayName("Should retrieve all students with pagination")
        void testGetAllStudents_Success() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            mockMvc.perform(get("/api/admin/students")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$.totalElements").isNumber())
                    .andExpect(jsonPath("$.content[0].studentNumber").exists())
                    .andExpect(jsonPath("$.content[0].firstName").exists())
                    .andExpect(jsonPath("$.content[0].lastName").exists());
        }

        @ParameterizedTest(name = "Page size={0} should return proper pagination")
        @ValueSource(ints = {1, 5, 10, 20})
        @DisplayName("Should paginate students correctly")
        void testGetAllStudents_Pagination(int pageSize) throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            mockMvc.perform(get("/api/admin/students")
                            .header("Authorization", "Bearer " + adminToken)
                            .param("page", "0")
                            .param("size", String.valueOf(pageSize)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size").value(pageSize))
                    .andExpect(jsonPath("$.number").value(0));
        }
    }

    @Nested
    @DisplayName("Admin View Students in Section Tests")
    class ViewStudentsInSectionTests {

        @Test
        @DisplayName("Should retrieve students enrolled in a specific section")
        void testAdminViewStudentsInSection_Success() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            // Get section ID for CS101-A which has enrollment from seed data
            Long sectionId = sectionRepository.findAll().stream()
                    .filter(s -> "CS101-A".equals(s.getSectionCode()))
                    .findFirst()
                    .map(s -> s.getId())
                    .orElse(1L);

            mockMvc.perform(get("/api/admin/sections/{id}/students", sectionId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber());
        }

        @Test
        @DisplayName("Should return 404 for non-existent section")
        void testAdminViewStudentsInSection_NotFound() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            mockMvc.perform(get("/api/admin/sections/{id}/students", 99999L)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Admin Section Management Tests")
    class SectionManagementTests {

        @Test
        @DisplayName("Should create new section successfully")
        void testCreateSection_Success() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            SectionCreateRequest request = SectionCreateRequest.builder()
                    .sectionCode("TEST-NEW")
                    .courseId(1L)
                    .roomId(1L)
                    .termId(2L)
                    .scheduleId(1L)
                    .maxSeats(30)
                    .build();

            mockMvc.perform(post("/api/admin/sections")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.sectionCode").value("TEST-NEW"))
                    .andExpect(jsonPath("$.maxSeats").value(30))
                    .andExpect(jsonPath("$.currentEnrollment").value(0));
        }

        @Test
        @DisplayName("Should update section successfully")
        void testUpdateSection_Success() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            // First create a section to update
            SectionCreateRequest createRequest = SectionCreateRequest.builder()
                    .sectionCode("UPDATE-TEST")
                    .courseId(1L)
                    .roomId(1L)
                    .termId(2L)
                    .scheduleId(2L)
                    .maxSeats(25)
                    .build();

            MvcResult createResult = mockMvc.perform(post("/api/admin/sections")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            SectionDto created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    SectionDto.class
            );

            // Update the section
            SectionUpdateRequest updateRequest = SectionUpdateRequest.builder()
                    .maxSeats(50)
                    .build();

            mockMvc.perform(put("/api/admin/sections/{id}", created.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.maxSeats").value(50));
        }

        @Test
        @DisplayName("Should delete section without enrollments")
        void testDeleteSection_Success() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            // Create a section to delete
            SectionCreateRequest createRequest = SectionCreateRequest.builder()
                    .sectionCode("DELETE-TEST")
                    .courseId(2L)
                    .roomId(2L)
                    .termId(2L)
                    .scheduleId(3L)
                    .maxSeats(20)
                    .build();

            MvcResult createResult = mockMvc.perform(post("/api/admin/sections")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            SectionDto created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    SectionDto.class
            );

            // Delete the section
            mockMvc.perform(delete("/api/admin/sections/{id}", created.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            // Verify it's deleted
            mockMvc.perform(get("/api/admin/sections/{id}/students", created.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Admin Term Management Tests")
    class TermManagementTests {

        @Test
        @DisplayName("Should retrieve all terms")
        void testGetAllTerms_Success() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            mockMvc.perform(get("/api/admin/terms")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(greaterThan(0))))
                    .andExpect(jsonPath("$[0].termName").exists())
                    .andExpect(jsonPath("$[0].startDate").exists())
                    .andExpect(jsonPath("$[0].endDate").exists());
        }

        @Test
        @DisplayName("Should create new term successfully")
        void testCreateTerm_Success() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            TermCreateRequest request = TermCreateRequest.builder()
                    .termName("Summer 2025")
                    .startDate(LocalDate.of(2025, 6, 1))
                    .endDate(LocalDate.of(2025, 8, 31))
                    .isActive(false)
                    .build();

            mockMvc.perform(post("/api/admin/terms")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.termName").value("Summer 2025"))
                    .andExpect(jsonPath("$.isActive").value(false));
        }
    }

    @Nested
    @DisplayName("Admin Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should return 401 for unauthenticated access")
        void testAdminEndpoint_WithoutToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/admin/students"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 403 for student trying to access admin endpoint")
        void testAdminEndpoint_AsStudent_Returns403() throws Exception {
            String studentToken = loginAndGetToken("jdoe", "student123");

            mockMvc.perform(get("/api/admin/students")
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest(name = "Admin endpoint {0} should be forbidden for students")
        @ValueSource(strings = {
                "/api/admin/students",
                "/api/admin/terms",
                "/api/admin/sections/1/students"
        })
        @DisplayName("Should return 403 for all admin endpoints when accessed by student")
        void testAllAdminEndpoints_AsStudent_Returns403(String endpoint) throws Exception {
            String studentToken = loginAndGetToken("jdoe", "student123");

            mockMvc.perform(get(endpoint)
                            .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 403 for student trying to create section")
        void testCreateSection_AsStudent_Returns403() throws Exception {
            String studentToken = loginAndGetToken("jdoe", "student123");

            SectionCreateRequest request = SectionCreateRequest.builder()
                    .sectionCode("FORBIDDEN")
                    .courseId(1L)
                    .roomId(1L)
                    .termId(1L)
                    .scheduleId(1L)
                    .maxSeats(30)
                    .build();

            mockMvc.perform(post("/api/admin/sections")
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should return 400 for invalid section creation request")
        void testCreateSection_InvalidRequest_Returns400() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            // Missing required fields
            String invalidRequest = "{}";

            mockMvc.perform(post("/api/admin/sections")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for invalid term dates")
        void testCreateTerm_InvalidDates_Returns400() throws Exception {
            String adminToken = loginAndGetToken("admin1", "admin123");

            // End date before start date
            TermCreateRequest request = TermCreateRequest.builder()
                    .termName("Invalid Term")
                    .startDate(LocalDate.of(2025, 12, 31))
                    .endDate(LocalDate.of(2025, 1, 1))
                    .isActive(false)
                    .build();

            mockMvc.perform(post("/api/admin/terms")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
