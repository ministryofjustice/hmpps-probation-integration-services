package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.sentence.NoteDetail
import uk.gov.justice.digital.hmpps.api.model.user.*
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.generateContactAlert
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

        val staff = transactionTemplate.execute {
            entityManager.find(
                uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Staff::class.java,
                user.staff!!.id
            )
        }!!

        val alertContacts = contactRepository.saveAll(
            listOf(
                ContactGenerator.generateContact(
                    person,
                    ContactGenerator.OTHER_CT,
                    ZonedDateTime.now().minusDays(2),
                    alert = true,
                    staff = staff,
                    notes = "Some notes about the other alert"
                ),
                ContactGenerator.generateContact(
                    person,
                    ContactGenerator.BREACH_CONTACT_TYPE,
                    ZonedDateTime.now().minusDays(1),
                    staff = staff,
                    alert = true,
                    description = "Description of first alert"
                ),
            )
        )

        contactAlertRepository.saveAll(alertContacts.map { generateContactAlert(it) })

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
                    listOf(),
                    null,
                    Staff(Name("Peter", null, "Parker"), "N01PEPA")
                ),
                UserAlert(
                    alertContacts[0].id,
                    UserAlertType("Non attendance contact type", editable = false),
                    "X000004",
                    Name(forename = "Forename", middleName = "Middle1 Middle2", surname = "Surname"),
                    LocalDate.now().minusDays(2),
                    null,
                    listOf(NoteDetail(0, note = "Some notes about the other alert", hasNoteBeenTruncated = false)),
                    null,
                    Staff(Name("Peter", null, "Parker"), "N01PEPA")
                )
            ), 2, 1, 0, 10
        )
        assertEquals(expected, response)

        val noteResponse = mockMvc
            .perform(MockMvcRequestBuilders.get("/alerts/${alertContacts[0].id}/notes/0").withUserToken(user.username))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.contentAsJson<UserAlert>()

        assertThat(noteResponse.alertNote).isEqualTo(NoteDetail(0, note = "Some notes about the other alert"))
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