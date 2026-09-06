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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ApprovalApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "demo-staff", roles = "STAFF")
    void staffCanRequestApprovalAndHistoryIsRecorded() throws Exception {
        mockMvc.perform(post("/api/work-orders/2/approval-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Ready for manager review\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestedBy").value("demo-staff"))
                .andExpect(jsonPath("$.decision").value("PENDING"))
                .andExpect(jsonPath("$.requestComment").value("Ready for manager review"));

        mockMvc.perform(get("/api/work-orders/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING_APPROVAL"));

        mockMvc.perform(get("/api/work-orders/2/approvals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].decision").value("PENDING"));

        mockMvc.perform(get("/api/work-orders/2/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("APPROVAL_REQUESTED"))
                .andExpect(jsonPath("$[0].actor").value("demo-staff"));
    }

    @Test
    @WithMockUser(username = "demo-staff", roles = "STAFF")
    void staffCannotDecideApproval() throws Exception {
        mockMvc.perform(post("/api/work-orders/2/approval-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/work-orders/2/approval-decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"APPROVE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "demo-admin", roles = "ADMIN")
    void adminApprovesThenApprovedWorkCanFinish() throws Exception {
        mockMvc.perform(post("/api/work-orders/2/approval-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"Please approve\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/work-orders/2/approval-decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"APPROVE\",\"comment\":\"Approved for completion\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/work-orders/2/approvals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].decision").value("APPROVED"))
                .andExpect(jsonPath("$[0].decidedBy").value("demo-admin"));

        mockMvc.perform(patch("/api/work-orders/2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"DONE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    @WithMockUser(username = "demo-admin", roles = "ADMIN")
    void rejectionReturnsWorkToInProgress() throws Exception {
        mockMvc.perform(post("/api/work-orders/2/approval-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/work-orders/2/approval-decision")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\":\"REJECT\",\"comment\":\"Need more evidence\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/work-orders/2/approvals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].decision").value("REJECTED"))
                .andExpect(jsonPath("$[0].decisionComment").value("Need more evidence"));
    }

    @Test
    @WithMockUser(username = "demo-staff", roles = "STAFF")
    void approvalRequestRequiresInProgressState() throws Exception {
        mockMvc.perform(post("/api/work-orders/1/approval-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_WORK_ORDER_TRANSITION"));
    }
}
