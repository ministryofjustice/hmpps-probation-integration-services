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
import uk.gov.justice.digital.hmpps.api.model.sentence.AssociationSummary
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITHOUT_NOTES
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_1500_CHAR_NOTE
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_NOTES
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LC_WITH_NOTES_WITHOUT_ADDED_BY
import uk.gov.justice.digital.hmpps.data.generator.LicenceConditionGenerator.LIC_COND_MAIN_CAT
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
            personSummary = PersonGenerator.OVERVIEW.toSummary(),
            contactTypeCode = code,
            associatedWithPerson = false,
            events = listOf(
                AssociationSummary(PersonGenerator.EVENT_2.id, "Pre-Sentence"),
                AssociationSummary(PersonGenerator.EVENT_1.id, "Default Sentence Type")
            ),
            licenceConditions = listOf(
                AssociationSummary(LC_WITHOUT_NOTES.id, LIC_COND_MAIN_CAT.description),
                AssociationSummary(LC_WITH_NOTES.id, LIC_COND_MAIN_CAT.description),
                AssociationSummary(LC_WITH_NOTES_WITHOUT_ADDED_BY.id, LIC_COND_MAIN_CAT.description),
                AssociationSummary(LC_WITH_1500_CHAR_NOTE.id, LIC_COND_MAIN_CAT.description)
            )
        )
        val response = mockMvc
            .perform(get("/appointment/${PersonGenerator.OVERVIEW.crn}/contact-type/${code}").withToken())
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<ContactTypeAssociation>()

        assertEquals(expected, response)
    }
}