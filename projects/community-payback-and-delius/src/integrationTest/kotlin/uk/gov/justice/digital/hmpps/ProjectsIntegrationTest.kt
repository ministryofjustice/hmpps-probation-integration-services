package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.DEFAULT_UPW_PROJECT_AVAILABILITY
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.UPW_PROJECT_1
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@SpringBootTest
@AutoConfigureMockMvc
class ProjectsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `can retrieve project details`() {
        mockMvc
            .get("/projects/${UPW_PROJECT_1.code}") { withToken() }
            .andExpect {
                status { is2xxSuccessful() }
                content {
                    json(
                        """
                            {
                              "name": "Default UPW Project",
                              "code": "N01P01",
                              "type": {
                                "name": "Group Placement",
                                "code": "G"
                              },
                              "team": {
                                "name": "N01 UPW Team",
                                "code": "N01UPW"
                              },
                              "provider": {
                                "name": "N01 Provider",
                                "code": "N01"
                              },
                              "location": {
                                "streetName": "Test Street",
                                "addressNumber": "123",
                                "townCity": "Town",
                                "postCode": "AB12CD"
                              },
                              "beneficiary": {
                                "name": "Beneficiary",
                                "contactName": "Joe Bloggs",
                                "emailAddress": "joebloggs@example.com",
                                "website": "https://example.com",
                                "location": {
                                  "streetName": "Test Street",
                                  "addressNumber": "123",
                                  "townCity": "Town",
                                  "postCode": "AB12CD"
                                }
                              },
                              "hiVisRequired": false,
                              "expectedEndDateExclusive": "${UPW_PROJECT_1.expectedEndDate}",
                              "availability": [
                                {
                                    "frequency": "Weekly",
                                    "dayOfWeek": "MONDAY",
                                    "startDateInclusive": "${DEFAULT_UPW_PROJECT_AVAILABILITY.startDate}",
                                    "endDateExclusive": "${DEFAULT_UPW_PROJECT_AVAILABILITY.endDate}"
                                }
                              ]
                            }
                        """.trimIndent(), JsonCompareMode.STRICT
                    )
                }
            }
    }

    @Test
    fun `returns 404 for unknown project code`() {
        mockMvc
            .get("/projects/DOESNOTEXIST") { withToken() }
            .andExpect { status { isNotFound() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).contains("Project with code of DOESNOTEXIST not found")
            }
    }
}
