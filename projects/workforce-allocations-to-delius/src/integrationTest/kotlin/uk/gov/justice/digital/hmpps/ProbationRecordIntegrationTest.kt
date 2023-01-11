package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.api.model.PrEvent
import uk.gov.justice.digital.hmpps.api.model.PrOffence
import uk.gov.justice.digital.hmpps.api.model.PrSentence
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.api.model.StaffMember
import uk.gov.justice.digital.hmpps.api.model.grade
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token
import java.time.LocalDate

@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class ProbationRecordIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var wireMockserver: WireMockServer

    @Test
    fun `get probation record unauthorised`() {
        mockMvc.perform(
            get("/allocation-demand/N452321/1/probation-record")
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `get probation record no results`() {

        mockMvc.perform(
            get("/allocation-demand/N452321/1/probation-record").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun `get probation record successful retrieval of active and inactive events`() {
        val person = PersonGenerator.DEFAULT
        val eventNumber = "2"
        val staff = StaffGenerator.STAFF_WITH_USER

        val res = mockMvc.perform(
            get("/allocation-demand/${person.crn}/$eventNumber/probation-record").withOAuth2Token(wireMockserver)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is2xxSuccessful)
            .andReturn().response.contentAsString

        val pr = objectMapper.readValue<ProbationRecord>(res)
        assertNotNull(pr)
        assertThat(pr.crn, equalTo(person.crn))
        assertThat(pr.name, equalTo(person.name()))
        assertThat(pr.event.number, equalTo(eventNumber))

        val sentence = PrSentence("Sentenced - In Custody", "12 Months", LocalDate.now().minusDays(2))
        val offences = listOf(
            PrOffence("A main offence", true),
            PrOffence("An additional offence", false)
        )
        val event = PrEvent(sentence, offences)

        assertThat(pr.activeEvents, hasItems(event))
        assertThat(
            pr.inactiveEvents,
            hasItems(event.copy(manager = StaffMember(staff.code, staff.name(), grade = staff.grade())))
        )
    }
}
