package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.appointment.ContactTypeAssociation
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.sentence.OrderSummary
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.personalDetails.PersonDetailsGenerator
import uk.gov.justice.digital.hmpps.service.toSummary
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AppointmentIntegrationTest {
    @Autowired
    internal lateinit var mockMvc: MockMvc

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(get("/appointment/D123456/contact-type/abc"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `no person records associated with contact type`() {

        val code = CreateAppointment.Type.PlannedDoorstepContactNS.code
        val expected = ContactTypeAssociation(
            PersonDetailsGenerator.PERSONAL_DETAILS.toSummary(),
            code,
            true
        )
        val response = mockMvc
            .perform(get("/appointment/${PersonDetailsGenerator.PERSONAL_DETAILS.crn}/contact-type/${code}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<ContactTypeAssociation>()

        assertEquals(expected, response)
    }

    @Test
    fun `person records associated with contact type`() {

        val code = CreateAppointment.Type.HomeVisitToCaseNS.code
        val expected = ContactTypeAssociation(
            PersonGenerator.OVERVIEW.toSummary(),
            code,
            false,
            listOf(
                OrderSummary(PersonGenerator.EVENT_2.id, "Pre-Sentence"),
                OrderSummary(PersonGenerator.EVENT_1.id, "Default Sentence Type")
            )

        )
        val response = mockMvc
            .perform(get("/appointment/${PersonGenerator.OVERVIEW.crn}/contact-type/${code}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<ContactTypeAssociation>()

        assertEquals(expected, response)
    }
}