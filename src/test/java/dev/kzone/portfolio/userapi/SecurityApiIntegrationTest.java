package dev.kzone.portfolio.userapi;

import dev.kzone.portfolio.userapi.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousApiRequestReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staffCanReadAndCreateOperationalData() throws Exception {
        mockMvc.perform(withBasicAuth(get("/api/users"), SecurityConfig.DEMO_STAFF_USERNAME, SecurityConfig.DEMO_STAFF_PASSWORD))
                .andExpect(status().isOk());

        mockMvc.perform(withBasicAuth(post("/api/work-orders"), SecurityConfig.DEMO_STAFF_USERNAME, SecurityConfig.DEMO_STAFF_PASSWORD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"권한 테스트 업무","customerName":"테스트 고객","assignee":"demo-staff"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void staffCannotDeleteDirectoryUser() throws Exception {
        mockMvc.perform(withBasicAuth(delete("/api/users/1"), SecurityConfig.DEMO_STAFF_USERNAME, SecurityConfig.DEMO_STAFF_PASSWORD))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanDeleteDirectoryUser() throws Exception {
        mockMvc.perform(withBasicAuth(delete("/api/users/1"), SecurityConfig.DEMO_ADMIN_USERNAME, SecurityConfig.DEMO_ADMIN_PASSWORD))
                .andExpect(status().isNoContent());
    }

    @Test
    void authMeReturnsCurrentPrincipalAndRoles() throws Exception {
        mockMvc.perform(withBasicAuth(get("/api/auth/me"), SecurityConfig.DEMO_ADMIN_USERNAME, SecurityConfig.DEMO_ADMIN_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(SecurityConfig.DEMO_ADMIN_USERNAME))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"))
                .andExpect(jsonPath("$.roles[1]").value("STAFF"));
    }

    @Test
    void swaggerDocumentRemainsPublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder withBasicAuth(
            MockHttpServletRequestBuilder request,
            String username,
            String password
    ) {
        String credentials = username + ":" + password;
        String token = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return request.header(HttpHeaders.AUTHORIZATION, "Basic " + token);
    }
}
