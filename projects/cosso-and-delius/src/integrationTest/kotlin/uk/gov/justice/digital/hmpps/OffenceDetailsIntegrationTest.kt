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
    fun `can get offence details from document uuid` () {
        val uuid = DocumentGenerator.DEFAULT_DOCUMENT_UUID
        val actual = mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isOk()} }
            .andReturn().response.contentAsJson<OffenceDetails>()

        assertThat(actual.mainOffence.code).isEqualTo(MainOffenceGenerator.DEFAULT_MAIN_OFFENCE.offence.mainCategoryCode)
        assertThat(actual.sentencingCourt).isEqualTo(CourtAppearanceGenerator.DEFAULT_COURT_APPEARANCE.court.courtName)
        assertThat(actual.sentenceDate).isEqualTo(DisposalGenerator.DEFAULT_DISPOSAL.disposalDate)
    }

    @Test
    fun `throws not found exception when event not found` () {
        val uuid = UUID.randomUUID()
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andExpect { jsonPath("$.message", org.hamcrest.Matchers.containsString("Event with")) }
    }

    @Test
    fun `throws not found exception when offence not found` () {
        val uuid = DocumentGenerator.MISSING_MAIN_OFFENCE_DOCUMENT_UUID
        val eventId = EventGenerator.MISSING_MAIN_OFFENCE_EVENT.eventId
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andExpect { jsonPath("$.message", org.hamcrest.Matchers.equalTo("Offence with eventId of ${eventId} not found")) }
    }

    @Test
    fun `throws not found exception court appearance not found` () {
        val uuid = DocumentGenerator.MISSING_COURT_APPEARANCE_DOCUMENT_UUID
        val eventId = EventGenerator.MISSING_COURT_APPEARANCE_EVENT.eventId
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andExpect { jsonPath("$.message", org.hamcrest.Matchers.equalTo("CourtAppearance with eventId of ${eventId} not found")) }
    }

    @Test
    fun `throws not found exception when disposal not found` () {
        val uuid = DocumentGenerator.MISSING_DISPOSAL_DOCUMENT_UUID
        val eventId = EventGenerator.MISSING_DISPOSAL_EVENT.eventId
        mockMvc.get("/offence-details/${uuid}") { withToken() }
            .andExpect { status { isNotFound() } }
            .andExpect { jsonPath("$.message", org.hamcrest.Matchers.equalTo("Disposal with eventId of ${eventId} not found")) }
    }
}