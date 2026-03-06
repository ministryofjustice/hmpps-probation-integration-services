package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.TestData.PersonData
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class SentencesIntegrationTest @Autowired constructor(private val mockMvc: MockMvc) {
    @Test
    fun `unknown crn returns not found`() {
        mockMvc.get("/person/DOESNOTEXIST/sentences") { withToken() }
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Person with CRN of DOESNOTEXIST not found") }
            }
    }

    @Test
    fun `get sentences`() {
        mockMvc.get("/person/${PersonData.DEFAULT.crn}/sentences") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(
                        """
                        {
                          "sentences": [
                            {
                              "type": "Community Order",
                              "startDate": "2024-01-01",
                              "expectedEndDate": "2025-06-01",
                              "requirements": [
                                {
                                  "type": "Court - Accredited Programme",
                                  "description": "Building Choices"
                                },
                                {
                                  "type": "Unpaid Work",
                                  "description": "Regular",
                                  "required": 10,
                                  "completed": 3,
                                  "unit": "HOURS"
                                },
                                {
                                  "type": "Rehabilitation Activity Requirement (RAR)",
                                  "description": "Rehabilitation Activity Requirement (RAR)",
                                  "required": 15,
                                  "completed": 2,
                                  "unit": "DAYS"
                                }
                              ],
                              "licenceConditions": [
                                {
                                  "type": "Alcohol Monitoring (Electronic Monitoring)",
                                  "description": "You must not drink any alcohol until [END DATE]. You will need to wear an electronic tag all the time so we can check this.",
                                  "startDate": "2024-02-01",
                                  "expectedEndDate": "2025-01-01"
                                }
                              ]
                            }
                          ]
                        }
                        """.trimIndent(),
                        JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `person with no sentences has empty list in response`() {
        mockMvc.get("/person/${PersonData.BASIC.crn}/sentences") { withToken() }.andExpect {
            status { isOk() }
            content { json("""{"sentences":[]}""", JsonCompareMode.STRICT) }
        }
    }
}
