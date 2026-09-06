package dev.kzone.portfolio.userapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "demo-staff", roles = "STAFF")
class AnalyticsApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsServerSideOperationsAnalytics() throws Exception {
        mockMvc.perform(get("/api/analytics/operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCustomers").value(5))
                .andExpect(jsonPath("$.activeCustomers").value(3))
                .andExpect(jsonPath("$.openWorkOrders").value(2))
                .andExpect(jsonPath("$.overdueWorkOrders").value(1))
                .andExpect(jsonPath("$.doneThisMonth").value(1))
                .andExpect(jsonPath("$.statusDistribution", hasSize(4)))
                .andExpect(jsonPath("$.statusDistribution[*].label", hasItem("IN_PROGRESS")))
                .andExpect(jsonPath("$.priorityDistribution", hasSize(4)))
                .andExpect(jsonPath("$.priorityDistribution[*].label", hasItem("URGENT")))
                .andExpect(jsonPath("$.workloadByAssignee", hasSize(2)))
                .andExpect(jsonPath("$.completedTrend.length()", greaterThanOrEqualTo(1)));
    }

    @Test
    @WithAnonymousUser
    void analyticsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/analytics/operations"))
                .andExpect(status().isUnauthorized());
    }
}
