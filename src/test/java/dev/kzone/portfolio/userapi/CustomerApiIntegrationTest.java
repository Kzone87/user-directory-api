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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser(username = "demo-admin", roles = "ADMIN")
class CustomerApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsAndFiltersCustomers() throws Exception {
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));

        mockMvc.perform(get("/api/customers").param("status", "LEAD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].companyName").value("Echo Studio"));

        mockMvc.perform(get("/api/customers").param("keyword", "beta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].companyName").value("Beta Tech"));
    }

    @Test
    void createsCustomerWithNormalizedEmail() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName":"Nova Works",
                                  "contactName":"Jamie",
                                  "email":"HELLO@EXAMPLE.COM",
                                  "phone":"010-9999-8888",
                                  "status":"LEAD",
                                  "memo":"Demo lead"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/customers/\\d+")))
                .andExpect(jsonPath("$.companyName").value("Nova Works"))
                .andExpect(jsonPath("$.email").value("hello@example.com"))
                .andExpect(jsonPath("$.status").value("LEAD"));
    }

    @Test
    void updatesCustomerStatusAndFields() throws Exception {
        mockMvc.perform(put("/api/customers/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName":"Echo Studio",
                                  "contactName":"Evan Jung",
                                  "email":"echo@example.com",
                                  "phone":"051-555-5555",
                                  "status":"ACTIVE",
                                  "memo":"Converted from lead"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.memo").value("Converted from lead"));
    }

    @Test
    void rejectsInvalidCustomerInput() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"companyName":"","email":"not-an-email","status":"ACTIVE"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void missingCustomerReturns404() throws Exception {
        mockMvc.perform(get("/api/customers/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CUSTOMER_NOT_FOUND"));
    }
}
