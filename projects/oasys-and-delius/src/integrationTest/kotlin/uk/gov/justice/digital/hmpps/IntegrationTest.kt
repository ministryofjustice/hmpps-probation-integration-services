package uk.gov.justice.digital.hmpps

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.CodeDescription
import uk.gov.justice.digital.hmpps.api.model.Registrations
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class IntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc
) {

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `registrations are correctly returned`() {
        val person = PersonGenerator.REGISTERED_PERSON
        val res = mockMvc.get("/probation-cases/${person.crn}/registrations") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andReturn().response.contentAsJson<Registrations>()

        assertThat(res.registrations, hasSize(2))

        val reg1 = res.registrations.first()
        assertThat(reg1.register, equalTo(CodeDescription("FLAG1", "Description of FLAG1")))
        assertThat(reg1.type, equalTo(CodeDescription("RT1", "Description of RT1")))
        assertThat(reg1.riskColour, equalTo("GREEN"))
        assertThat(reg1.startDate, equalTo(LocalDate.now()))
        assertThat(reg1.nextReviewDate, equalTo(LocalDate.now().plusMonths(6)))
        assertThat(reg1.reviewPeriodMonths, equalTo(6))
        assertThat(reg1.registeringTeam, equalTo(CodeDescription("N01UAT", "Description of N01UAT")))
        assertTrue(reg1.registeringOfficer.isUnallocated)
        assertThat(reg1.registeringProbationArea, equalTo(CodeDescription("N01", "Description of N01")))
        assertThat(reg1.registerLevel, equalTo(CodeDescription("LEV1", "Description of LEV1")))
        assertThat(reg1.registerCategory, equalTo(CodeDescription("CAT1", "Description of CAT1")))
        assertTrue(reg1.warnUser)
        assertTrue(reg1.active)
        assertThat(reg1.registrationReviews, hasSize(1))

        val reg2 = res.registrations[1]
        assertNull(reg2.register)
        assertThat(reg2.type, equalTo(CodeDescription("AN1", "Description of AN1")))
        assertNull(reg2.riskColour)
        assertNull(reg2.reviewPeriodMonths)
        assertFalse(reg2.registeringOfficer.isUnallocated)
        assertNull(reg2.registerLevel)
        assertNull(reg2.registerCategory)
        assertFalse(reg2.warnUser)
        assertTrue(reg2.active)
    }

    @Test
    fun `returns standaloneOrderOnly true for a standalone case (restrictive rqmnt)`() {
        val person = PersonGenerator.STANDALONE_ONLY_PERSON
        mockMvc.get("/probation-cases/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andExpect { jsonPath("$.standaloneOrderOnly") { value(true) } }
    }

    @Test
    fun `returns standaloneOrderOnly true for a standalone case (upw rqmnt)`() {
        val person = PersonGenerator.UPW_ONLY_PERSON
        mockMvc.get("/probation-cases/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andExpect { jsonPath("$.standaloneOrderOnly") { value(true) } }
    }

    @Test
    fun `returns standaloneOrderOnly false when person has no events`() {
        val person = PersonGenerator.NO_EVENTS_PERSON
        mockMvc.get("/probation-cases/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andExpect { jsonPath("$.standaloneOrderOnly") { value(false) } }
    }

    @Test
    fun `returns standaloneOrderOnly false when event has no disposal`() {
        val person = PersonGenerator.NO_DISPOSAL_PERSON
        mockMvc.get("/probation-cases/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andExpect { jsonPath("$.standaloneOrderOnly") { value(false) } }
    }

    @Test
    fun `returns standaloneOrderOnly false when case has one upw requirement and one non upw or restrictive requirement`() {
        val person = PersonGenerator.MIXED_ORDERS_PERSON
        mockMvc.get("/probation-cases/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andExpect { jsonPath("$.standaloneOrderOnly") { value(false) } }
    }

    @Test
    fun `returns standaloneOrderOnly false when disposal has no requirements`() {
        val person = PersonGenerator.EMPTY_REQUIREMENTS_PERSON
        mockMvc.get("/probation-cases/${person.crn}") { withToken() }
            .andExpect { status { is2xxSuccessful() } }
            .andExpect { jsonPath("$.standaloneOrderOnly") { value(false) } }
    }
}
