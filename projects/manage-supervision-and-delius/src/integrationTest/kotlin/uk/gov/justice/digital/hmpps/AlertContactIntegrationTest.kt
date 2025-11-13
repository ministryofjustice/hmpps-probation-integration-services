package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.user.*
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_1
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator.STAFF_USER_2
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
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
                    Name(forename = "Forename", middleName = "Middle1 Middle2", surname = "Surname"),
                    LocalDate.now().minusDays(1),
                    "Description of first alert",
                    null,
                    Staff(Name("John", null, "Smith"), "N01BDT1")
                ),
                UserAlert(
                    alertContacts[0].id,
                    UserAlertType("Non attendance contact type", editable = false),
                    "X000004",
                    Name(forename = "Forename", middleName = "Middle1 Middle2", surname = "Surname"),
                    LocalDate.now().minusDays(2),
                    null,
                    "Some notes about the other alert",
                    Staff(Name("Limited", null, "Access"), "N01BDT3")
                )
            ), 2, 1, 0, 10
        )
        assertEquals(expected, response)
    }

    @Test
    fun `unauthorized status returned when clearing alerts without a token`() {
        mockMvc
            .perform(MockMvcRequestBuilders.put("/alerts"))
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
    }

    @Test
    fun `bad request if no username present in token`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/alerts")
                    .withToken()
                    .withJson(ClearAlerts(listOf(IdGenerator.getAndIncrement())))
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `bad request if no alerts sent for clearing`() {
        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/alerts")
                    .withUserToken("no-alert-ids")
                    .withJson(ClearAlerts(emptyList()))
            ).andExpect(MockMvcResultMatchers.status().isBadRequest)
    }

    @Test
    fun `no contact updated if id does not exist`() {
        val user = STAFF_USER_1
        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/alerts")
                    .withUserToken(user.username)
                    .withJson(ClearAlerts(listOf(IdGenerator.getAndIncrement())))
            ).andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `no contact updated if user is not case manager`() {
        val user = STAFF_USER_2
        val person = PersonGenerator.OVERVIEW

        val contact = contactRepository.save(
            ContactGenerator.generateContact(
                person,
                ContactGenerator.BREACH_CONTACT_TYPE,
                ZonedDateTime.now().minusDays(5),
                alert = true,
                description = "Description of first alert"
            )
        )

        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/alerts")
                    .withUserToken(user.username)
                    .withJson(ClearAlerts(listOf(IdGenerator.getAndIncrement())))
            ).andExpect(MockMvcResultMatchers.status().isOk)

        assertThat(contact.alert).isTrue
        contactRepository.save(contact.apply { alert = false })
    }

    @Test
    fun `can clear alerts for username`() {
        val user = STAFF_USER_1
        val person = PersonGenerator.OVERVIEW

        val alertContacts = contactRepository.saveAll(
            listOf(
                ContactGenerator.generateContact(
                    person,
                    ContactGenerator.OTHER_CT,
                    ZonedDateTime.now().minusDays(7),
                    alert = true,
                    staff = ContactGenerator.LIMITED_ACCESS_STAFF,
                    notes = "Some notes about the other alert"
                ),
                ContactGenerator.generateContact(
                    person,
                    ContactGenerator.BREACH_CONTACT_TYPE,
                    ZonedDateTime.now().minusDays(5),
                    alert = true,
                    description = "Description of first alert"
                ),
            )
        )

        mockMvc
            .perform(
                MockMvcRequestBuilders.put("/alerts")
                    .withUserToken(user.username)
                    .withJson(ClearAlerts(alertContacts.map { it.id }))
            )
            .andExpect(MockMvcResultMatchers.status().isOk)

        contactRepository.findAllById(alertContacts.map { it.id }).forEach {
            assertThat(it.alert).isFalse
            assertThat(it.notes).contains("Alert cleared from MPOP")
        }
    }
}