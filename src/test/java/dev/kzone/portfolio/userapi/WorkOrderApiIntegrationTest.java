package dev.kzone.portfolio.userapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "demo-admin", roles = "ADMIN")
class WorkOrderApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsSeedWorkOrdersAndFiltersByStatus() throws Exception {
        mockMvc.perform(get("/api/work-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        mockMvc.perform(get("/api/work-orders").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerName").value("에이스테크"));
    }

    @Test
    void createsWorkOrderAsReceived() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"신규 데이터 검수","customerName":"테스트상사","assignee":"Kim Developer"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/work-orders/\\d+")))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void acceptsAllowedStatusTransition() throws Exception {
        mockMvc.perform(patch("/api/work-orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void rejectsSkippedStatusTransition() throws Exception {
        mockMvc.perform(patch("/api/work-orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_WORK_ORDER_TRANSITION"));
    }

    @Test
    void terminalStatusCannotTransitionAgain() throws Exception {
        mockMvc.perform(patch("/api/work-orders/3/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isConflict());
    }
}
