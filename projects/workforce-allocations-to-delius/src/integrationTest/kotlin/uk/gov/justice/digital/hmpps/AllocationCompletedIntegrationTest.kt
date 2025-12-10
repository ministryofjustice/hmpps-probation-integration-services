package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.api.model.Mappings
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class AllocationCompletedIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val contactRepository: ContactRepository
) {

    @Test
    fun `successful response`() {
        contactRepository.save(ContactGenerator.INITIAL_APPOINTMENT)

        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        val contact = ContactGenerator.INITIAL_APPOINTMENT
        mockMvc.get("/allocation-completed/details") {
            withToken()
            param("crn", person.crn)
            param("eventNumber", event.number)
            param("staffCode", staff.code)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.crn") { value(person.crn) }
                jsonPath("$.name.forename") { value(person.forename) }
                jsonPath("$.name.middleName") { value(person.secondName) }
                jsonPath("$.name.surname") { value(person.surname) }
                jsonPath("$.initialAppointment.date") { value(contact.date.toString()) }
                jsonPath("$.type") { value("CUSTODY") }
                jsonPath("$.staff.code") { value(staff.code) }
                jsonPath("$.staff.email") { doesNotExist() }
            }
    }

    @Test
    fun `allocation order manager successful response_new`() {
        val person = PersonGenerator.DEFAULT
        val event = EventGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        mockMvc.get("/allocation-completed/order-manager") {
            withToken()
            param("crn", person.crn)
            param("eventNumber", event.number)
        }
            .andExpect {
                status { is2xxSuccessful() }
                jsonPath("$.code") { value(staff.code) }
                jsonPath("$.name.forename") { value(staff.forename) }
                jsonPath("$.name.surname") { value(staff.surname) }
                jsonPath("$.grade") { value(staff.grade?.code?.let { Mappings.toAllocationsGradeCode[it] }) }
                jsonPath("$.teamCode") { value(team.code) }
            }
    }
}
