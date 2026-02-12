package uk.gov.justice.digital.hmpps

import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.util.UUID

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OffenceDetailsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `can get offence details from document uuid`() {
        val uuid = DocumentGenerator.DEFAULT_DOCUMENT_UUID
        val sentenceDate = LocalDate.now().minusDays(7).toString()
        val requirementStartDate = LocalDate.now().minusDays(6).toString()
        val expectedResponse = """
            {
              "mainOffence": {
                "code": "101",
                "description": "Theft"
              },
              "additionalOffences": [
                {
                  "code": "101",
                  "description": "Shoplifting"
                }
              ],
              "sentencingCourt": "Warwick Magistrates Court",
              "sentenceDate": "${sentenceDate}",
              "sentenceImposed": {
                "code": "PR",
                "description": "Probation"
              },
              "requirementsImposed": [
                {
                  "id": 1000031,
                  "startDate": "${requirementStartDate}",
                  "mainCategory": "Probation",
                  "length": 2,
                  "lengthUnit": "Months",
                  "subCategory": "Probation2",
                  "secondaryLength": 1,
                  "secondaryLengthUnit": "Days"
                }
              ],
              "sentence": {
                "length": 1,
                "lengthUnits": "Months",
                "type": "Probation",
                "secondLength": 2,
                "secondLengthUnits": "Days"
              }
            }
            """
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect {
                status { isOk() }
                content {
                    json(expectedResponse, JsonCompareMode.STRICT)
                }
            }
    }

    @Test
    fun `throws not found exception when event not found`() {
        val uuid = UUID.randomUUID()
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andExpect { jsonPath("$.message", equalTo("DocumentEntity with UUID of ${uuid} not found")) }
    }

    @Test
    fun `throws not found exception when offence not found`() {
        val uuid = DocumentGenerator.MISSING_MAIN_OFFENCE_DOCUMENT_UUID
        val eventId = EventGenerator.MISSING_MAIN_OFFENCE_EVENT.eventId
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andExpect {
                jsonPath(
                    "$.message",
                    equalTo("Offence with eventId of ${eventId} not found")
                )
            }
    }

    @Test
    fun `throws not found exception court appearance not found`() {
        val uuid = DocumentGenerator.MISSING_COURT_APPEARANCE_DOCUMENT_UUID
        val eventId = EventGenerator.MISSING_COURT_APPEARANCE_EVENT.eventId
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andExpect {
                jsonPath(
                    "$.message",
                    equalTo("CourtAppearance with eventId of ${eventId} not found")
                )
            }
    }

    @Test
    fun `throws not found exception when disposal not found`() {
        val uuid = DocumentGenerator.MISSING_DISPOSAL_DOCUMENT_UUID
        val eventId = EventGenerator.MISSING_DISPOSAL_EVENT.eventId
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andExpect {
                jsonPath(
                    "$.message",
                    equalTo("Disposal with eventId of ${eventId} not found")
                )
            }
    }
}
