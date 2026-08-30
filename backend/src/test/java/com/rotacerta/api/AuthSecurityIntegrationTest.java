package com.rotacerta.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void protectedEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCanLoginAndAccessAdministrativeApi() throws Exception {
        String accessToken = login("admin@rotacerta.local", "Admin@123")
                .path("accessToken").asText();

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void customerCanUseOwnPortalButCannotAccessAdminDashboard() throws Exception {
        JsonNode login = login("customer@rotacerta.local", "Customer@123");
        String accessToken = login.path("accessToken").asText();

        mockMvc.perform(get("/api/customer-portal/orders")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void driverCanUseDriverPortalButCannotAccessAdminDashboard() throws Exception {
        JsonNode login = login("driver@rotacerta.local", "Driver@123");
        String accessToken = login.path("accessToken").asText();

        mockMvc.perform(get("/api/driver-portal/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void refreshTokenIsRotatedAndOldTokenCannotBeReused() throws Exception {
        JsonNode login = login("admin@rotacerta.local", "Admin@123");
        String refreshToken = login.path("refreshToken").asText();

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        JsonNode refreshed = objectMapper.readTree(refreshResult.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertNotEquals(refreshToken, refreshed.path("refreshToken").asText());

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized());
    }

    private JsonNode login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
