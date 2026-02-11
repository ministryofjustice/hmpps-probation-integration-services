package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.data.document.Event
import uk.gov.justice.digital.hmpps.data.generator.CourtAppearanceGenerator
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.DocumentGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.MainOffenceGenerator
import uk.gov.justice.digital.hmpps.entity.MainOffence
import uk.gov.justice.digital.hmpps.model.OffenceDetails
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.util.UUID

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class OffenceDetailsIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {
    @Test
    fun `can get offence details from document uuid`() {
        val uuid = DocumentGenerator.DEFAULT_DOCUMENT_UUID
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
              "sentenceDate": "2026-02-04",
              "sentenceImposed": {
                "code": "PR",
                "description": "Probation"
              },
              "requirementsImposed": [
                {
                  "id": 1000031,
                  "startDate": "2026-02-05",
                  "requirementTypeMainCategoryDescription": "Probation",
                  "requirementLength": 2,
                  "requirementLengthUnits": "Months",
                  "requirementTypeSubCategoryDescription": "Probation2",
                  "secondaryRequirementLength": 1,
                  "secondaryRequirementLengthUnits": "Days"
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
        val actual = mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isOk() } }
            .andReturn().response.contentAsString
        assertThat(actual).isNotEmpty
        JSONAssert.assertEquals(expectedResponse, actual, true)
    }

    @Test
    fun `throws not found exception when event not found`() {
        val uuid = UUID.randomUUID()
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andExpect { jsonPath("$.message", org.hamcrest.Matchers.containsString("Event with")) }
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
                    org.hamcrest.Matchers.equalTo("Offence with eventId of ${eventId} not found")
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
                    org.hamcrest.Matchers.equalTo("CourtAppearance with eventId of ${eventId} not found")
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
                    org.hamcrest.Matchers.equalTo("Disposal with eventId of ${eventId} not found")
                )
            }
    }
}
