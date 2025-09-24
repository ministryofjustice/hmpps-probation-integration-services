package uk.gov.justice.digital.hmpps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.user.Staff
import uk.gov.justice.digital.hmpps.api.model.user.UserAlert
import uk.gov.justice.digital.hmpps.api.model.user.UserAlertType
import uk.gov.justice.digital.hmpps.api.model.user.UserAlerts
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import java.time.LocalDate
import java.time.ZonedDateTime

class AlertContactIntegrationTest : IntegrationTestBase() {

    @Test
    fun `unauthorized status returned`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/alerts"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `no alerts`() {
        mockMvc
            .perform(MockMvcRequestBuilders.get("/alerts").withUserToken("no-alerts"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `return alerts for username`() {
        val user = STAFF_USER_1
        val person = PersonGenerator.OVERVIEW

        val alertContacts = contactRepository.saveAll(
            listOf(
                ContactGenerator.generateContact(
                    person,
                    ContactGenerator.OTHER_CT,
                    ZonedDateTime.now().minusDays(2),
                    alert = true,
                    staff = ContactGenerator.LIMITED_ACCESS_STAFF,
                    notes = "Some notes about the other alert"
                ),
                ContactGenerator.generateContact(
                    person,
                    ContactGenerator.BREACH_CONTACT_TYPE,
                    ZonedDateTime.now().minusDays(1),
                    alert = true,
                    description = "Description of first alert"
                ),
            )
        )

        val response = mockMvc
            .perform(MockMvcRequestBuilders.get("/alerts").withUserToken(user.username))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<UserAlerts>()

        val expected = UserAlerts(
            listOf(
                UserAlert(
                    alertContacts[1].id,
                    UserAlertType("Breach Contact Type", editable = true),
                    "X000004",
                    LocalDate.now().minusDays(1),
                    "Description of first alert",
                    null,
                    Staff(Name("John", null, "Smith"), "N01BDT1")
                ),
                UserAlert(
                    alertContacts[0].id,
                    UserAlertType("Non attendance contact type", editable = false),
                    "X000004",
                    LocalDate.now().minusDays(2),
                    null,
                    "Some notes about the other alert",
                    Staff(Name("Limited", null, "Access"), "N01BDT3")
                )
            ), 2, 1, 0, 10
        )
        assertEquals(expected, response)
    }
}