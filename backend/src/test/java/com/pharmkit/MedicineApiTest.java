package com.pharmkit;

import com.pharmkit.service.UserService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "test@ilactakip.local")
class MedicineApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService users;

    @BeforeEach
    void seedUser() {
        users.ensureLocalUser("test-google-id", "Test", "Kullanıcı", "test@ilactakip.local");
    }

    @Test
    void createAndListMedicine() throws Exception {
        mockMvc.perform(post("/api/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Parol",
                                  "purchasedOn": "2026-09-01",
                                  "expiresOn": "2027-09-01",
                                  "intakeSchedule": "Günde 3 kez",
                                  "homeLocation": "Banyo dolabı"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Parol"));

        mockMvc.perform(get("/api/medicines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Parol')].intakeSchedule", hasItem("Günde 3 kez")))
                .andExpect(jsonPath("$[?(@.name=='Parol')].homeLocation", hasItem("Banyo dolabı")));
    }

    @Test
    void addDoseToMedicine() throws Exception {
        String body = mockMvc.perform(post("/api/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Aspirin",
                                  "purchasedOn": "2026-09-01",
                                  "expiresOn": "2027-09-01",
                                  "intakeSchedule": "Günde 1 kez",
                                  "homeLocation": "Mutfak"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer id = JsonPath.read(body, "$.id");

        mockMvc.perform(post("/api/medicines/" + id + "/doses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "takenAt": "2026-09-27T10:15:00" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.medicineId").value(id))
                .andExpect(jsonPath("$.takenAt").value("2026-09-27T10:15:00"));

        mockMvc.perform(get("/api/medicines/" + id + "/doses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void deleteDoseRemovesItFromMedicine() throws Exception {
        String body = mockMvc.perform(post("/api/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Nurofen",
                                  "purchasedOn": "2026-09-01",
                                  "expiresOn": "2027-09-01",
                                  "intakeSchedule": "Gerektiğinde",
                                  "homeLocation": "Çekmece"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer medicineId = JsonPath.read(body, "$.id");

        String doseBody = mockMvc.perform(post("/api/medicines/" + medicineId + "/doses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "takenAt": "2026-10-01T08:00:00" }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Integer doseId = JsonPath.read(doseBody, "$.id");

        mockMvc.perform(delete("/api/doses/" + doseId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/medicines/" + medicineId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doseCount").value(0))
                .andExpect(jsonPath("$.doses", hasSize(0)))
                .andExpect(jsonPath("$.lastTakenAt").value(nullValue()));
    }

    @Test
    void rejectExpiryBeforePurchase() throws Exception {
        mockMvc.perform(post("/api/medicines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Vitamin D",
                                  "purchasedOn": "2026-09-01",
                                  "expiresOn": "2026-08-01",
                                  "intakeSchedule": "Günde 1 kez",
                                  "homeLocation": "Mutfak"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
