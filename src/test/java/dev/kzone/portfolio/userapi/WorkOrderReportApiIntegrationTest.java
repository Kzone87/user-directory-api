package dev.kzone.portfolio.userapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class WorkOrderReportApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "demo-admin", roles = "ADMIN")
    void adminDownloadsSpreadsheetSafeCsvReport() throws Exception {
        mockMvc.perform(post("/api/work-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"=SUM(1,1)","customerId":1,"assignee":"Kim Developer","priority":"NORMAL"}
                                """))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(get("/api/reports/work-orders.csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("work-orders-report.csv")))
                .andReturn();

        String csv = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(csv.startsWith("\uFEFFID,Title,Customer"));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("'=SUM(1,1)"));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("Account setup request"));
    }

    @Test
    @WithMockUser(username = "demo-admin", roles = "ADMIN")
    void adminDownloadsXlsxAndCanFilterByDate() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/reports/work-orders.xlsx")
                        .param("from", "2026-09-01")
                        .param("to", "2026-09-30"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("work-orders-report.xlsx")))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        org.junit.jupiter.api.Assertions.assertTrue(bytes.length > 100);
        org.junit.jupiter.api.Assertions.assertEquals('P', bytes[0]);
        org.junit.jupiter.api.Assertions.assertEquals('K', bytes[1]);
    }

    @Test
    @WithMockUser(username = "demo-staff", roles = "STAFF")
    void staffCannotDownloadOperationalReport() throws Exception {
        mockMvc.perform(get("/api/reports/work-orders.csv"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "demo-admin", roles = "ADMIN")
    void invalidReportRangeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/reports/work-orders.csv")
                        .param("from", "2026-09-30")
                        .param("to", "2026-09-01"))
                .andExpect(status().isBadRequest());
    }
}
