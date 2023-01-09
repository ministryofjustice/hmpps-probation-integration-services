package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.security.withOAuth2Token

@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AllocationCompletedIntegrationTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var wireMockserver: WireMockServer

    @Test
    fun `successful response`() {
        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        val contact = ContactGenerator.INITIAL_APPOINTMENT
        mockMvc.perform(
            get("/allocation-completed/details").withOAuth2Token(wireMockserver)
                .param("crn", person.crn)
                .param("eventNumber", event.number)
                .param("staffCode", staff.code)
        )
            .andExpect(status().is2xxSuccessful)
            .andExpect(jsonPath("$.crn").value(person.crn))
            .andExpect(jsonPath("$.name.forename").value(person.forename))
            .andExpect(jsonPath("$.name.middleName").value(person.secondName))
            .andExpect(jsonPath("$.name.surname").value(person.surname))
            .andExpect(jsonPath("$.initialAppointment.date").value(contact.date.toLocalDate().toString()))
            .andExpect(jsonPath("$.type").value("CUSTODY"))
            .andExpect(jsonPath("$.staff.code").value(staff.code))
            .andExpect(jsonPath("$.staff.email").doesNotExist())
    }
}
