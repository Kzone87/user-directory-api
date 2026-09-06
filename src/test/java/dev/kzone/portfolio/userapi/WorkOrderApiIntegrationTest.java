package dev.kzone.portfolio.userapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

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
    void listsSeedWorkOrdersWithPriorityAndDueDate() throws Exception {
        mockMvc.perform(get("/api/work-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].priority").value("URGENT"))
                .andExpect(jsonPath("$[0].dueDate").value(LocalDate.now().plusDays(1).toString()));

        mockMvc.perform(get("/api/work-orders").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value(2))
                .andExpect(jsonPath("$[0].customerName").value("Beta Tech"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"))
                .andExpect(jsonPath("$[0].dueDate").value(LocalDate.now().minusDays(1).toString()));
    }

    @Test
    void createsWorkOrderWithPlanningFields() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"New data review","customerId":5,"assignee":"Kim Developer","priority":"HIGH","dueDate":"2026-09-12"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/work-orders/\\d+")))
                .andExpect(jsonPath("$.customerId").value(5))
                .andExpect(jsonPath("$.customerName").value("Echo Studio"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.dueDate").value("2026-09-12"))
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    @Test
    void priorityIsRequired() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Missing priority","customerId":1,"assignee":"Kim Developer"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void rejectsWorkOrderForMissingCustomer() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Invalid customer order","customerId":9999,"assignee":"Kim Developer","priority":"NORMAL"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void listsSeedActivityHistoryNewestFirst() throws Exception {
        mockMvc.perform(get("/api/work-orders/3/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].actor").value("demo-admin"))
                .andExpect(jsonPath("$[0].action").value("STATUS_CHANGED"))
                .andExpect(jsonPath("$[0].fromStatus").value("IN_PROGRESS"))
                .andExpect(jsonPath("$[0].toStatus").value("DONE"));
    }

    @Test
    void acceptsAllowedStatusTransitionAndRecordsActor() throws Exception {
        mockMvc.perform(patch("/api/work-orders/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mockMvc.perform(get("/api/work-orders/1/activities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].actor").value("demo-admin"))
                .andExpect(jsonPath("$[0].action").value("STATUS_CHANGED"));
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
