package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class CaseControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `personal details 404`() {
        mockMvc
            .perform(get("/case/DOESNOTEXIST/personal-details").withToken())
            .andExpect(status().isNotFound)
    }

    @Test
    fun `personal details success`() {
        mockMvc
            .perform(get("/case/${TestData.PERSON.crn}/personal-details").withToken())
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "crn": "${TestData.PERSON.crn}",
                      "name": {
                        "forename": "Forename",
                        "middleNames": "MiddleName",
                        "surname": "Surname"
                      },
                      "dateOfBirth": "${LocalDate.now().minusYears(45).minusMonths(6)}",
                      "age": "45 years, 6 months",
                      "sex": {
                        "code": "M",
                        "description": "Male"
                      },
                      "ethnicity": {
                        "code": "A9",
                        "description": "Asian or Asian British: Other"
                      },
                      "probationPractitioner": {
                        "name": {
                          "forename": "Forename",
                          "surname": "Surname"
                        },
                        "email": "test@example.com"
                      },
                      "probationDeliveryUnit": {
                        "code": "PDU1",
                        "description": "Test PDU"
                      }
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT,
                )
            )
    }
}
