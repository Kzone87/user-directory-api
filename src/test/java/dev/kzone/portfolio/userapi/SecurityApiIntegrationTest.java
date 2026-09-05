package dev.kzone.portfolio.userapi;

import dev.kzone.portfolio.userapi.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
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
        mockMvc.perform(get("/api/users")
                        .with(httpBasic(SecurityConfig.DEMO_STAFF_USERNAME, SecurityConfig.DEMO_STAFF_PASSWORD)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/work-orders")
                        .with(httpBasic(SecurityConfig.DEMO_STAFF_USERNAME, SecurityConfig.DEMO_STAFF_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"권한 테스트 업무","customerName":"테스트 고객","assignee":"demo-staff"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void staffCannotDeleteDirectoryUser() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(httpBasic(SecurityConfig.DEMO_STAFF_USERNAME, SecurityConfig.DEMO_STAFF_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanDeleteDirectoryUser() throws Exception {
        mockMvc.perform(delete("/api/users/1")
                        .with(httpBasic(SecurityConfig.DEMO_ADMIN_USERNAME, SecurityConfig.DEMO_ADMIN_PASSWORD)))
                .andExpect(status().isNoContent());
    }

    @Test
    void authMeReturnsCurrentPrincipalAndRoles() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .with(httpBasic(SecurityConfig.DEMO_ADMIN_USERNAME, SecurityConfig.DEMO_ADMIN_PASSWORD)))
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
}
