package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.data.TestData.REQUIREMENTS
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class AppointmentControllerIntegrationTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `exception when end date before start date`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = listOf(1L),
                        licenceConditionIds = listOf(2L),
                        fromDate = LocalDate.of(2030, 12, 31),
                        toDate = LocalDate.of(2030, 12, 1),
                    )
                )
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `exception when no requirement or licence condition ids provided`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = emptyList(),
                        licenceConditionIds = emptyList(),
                        fromDate = LocalDate.of(2030, 1, 1),
                        toDate = LocalDate.of(2030, 12, 31),
                    )
                )
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `exception when providing over 500 requirement ids`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = (1..1001).map { it.toLong() },
                        licenceConditionIds = emptyList(),
                        fromDate = LocalDate.of(2030, 1, 1),
                        toDate = LocalDate.of(2030, 12, 31),
                    )
                )
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `exception when providing over 500 licence condition ids`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = emptyList(),
                        licenceConditionIds = (1..1001).map { it.toLong() },
                        fromDate = LocalDate.of(2030, 1, 1),
                        toDate = LocalDate.of(2030, 12, 31),
                    )
                )
            ).andExpect(status().isBadRequest)
    }

    @Test
    fun `get appointments success`() {
        mockMvc
            .perform(
                post("/appointments/search").withToken().withJson(
                    GetAppointmentsRequest(
                        requirementIds = TestData.REQUIREMENTS.map { it.id },
                        licenceConditionIds = TestData.LICENCE_CONDITIONS.map { it.id },
                        fromDate = LocalDate.of(2030, 1, 1),
                        toDate = LocalDate.of(2030, 12, 31),
                    )
                )
            )
            .andExpect(status().isOk)
            .andExpect(
                content().json(
                    """
                    {
                      "content": {
                        "A000001": [
                            {
                              "crn": "A000001",
                              "reference": "${TestData.APPOINTMENTS[0].externalReference?.takeLast(36)}",
                              "requirementId": ${TestData.REQUIREMENTS[0].id},
                              "date": "${TestData.APPOINTMENTS[0].date}",
                              "staff": {
                                "name": {
                                  "forename": "Forename",
                                  "surname": "Surname"
                                },
                                "code": "STAFF01"
                              },
                              "team": {
                                "code": "TEAM01",
                                "description": "Test Team"
                              },
                              "notes": "Some appointment notes",
                              "sensitive": false
                            },
                            {
                              "crn": "A000001",
                              "reference": "${TestData.APPOINTMENTS[2].externalReference?.takeLast(36)}",
                              "licenceConditionId": ${TestData.LICENCE_CONDITIONS[0].id},
                              "date": "${TestData.APPOINTMENTS[2].date}",
                              "staff": {
                                "name": {
                                  "forename": "Forename",
                                  "surname": "Surname"
                                },
                                "code": "STAFF01"
                              },
                              "team": {
                                "code": "TEAM01",
                                "description": "Test Team"
                              },
                              "notes": "Some appointment notes",
                              "sensitive": false
                            },
                            {
                              "crn": "A000001",
                              "reference": "${TestData.APPOINTMENTS[1].externalReference?.takeLast(36)}",
                              "requirementId": ${TestData.REQUIREMENTS[1].id},
                              "date": "${TestData.APPOINTMENTS[1].date}",
                              "staff": {
                                "name": {
                                  "forename": "Forename",
                                  "surname": "Surname"
                                },
                                "code": "STAFF01"
                              },
                              "team": {
                                "code": "TEAM01",
                                "description": "Test Team"
                              },
                              "notes": "Some appointment notes",
                              "sensitive": false
                            },
                            {
                              "crn": "A000001",
                              "reference": "${TestData.APPOINTMENTS[3].externalReference?.takeLast(36)}",
                              "licenceConditionId": ${TestData.LICENCE_CONDITIONS[1].id},
                              "date": "${TestData.APPOINTMENTS[3].date}",
                              "staff": {
                                "name": {
                                  "forename": "Forename",
                                  "surname": "Surname"
                                },
                                "code": "STAFF01"
                              },
                              "team": {
                                "code": "TEAM01",
                                "description": "Test Team"
                              },
                              "notes": "Some appointment notes",
                              "sensitive": false
                            }
                        ]
                      }
                    }
                    """.trimIndent(),
                    JsonCompareMode.STRICT,
                )
            )
    }

    @Test
    fun `can create an appointment`() {
        val appointmentReference = UUID.randomUUID()
        mockMvc
            .perform(
                post("/appointments").withToken().withJson(
                    CreateAppointmentsRequest(
                        listOf(
                            CreateAppointmentRequest(
                                appointmentReference,
                                REQUIREMENTS[2].id,
                                null,
                                LocalDate.now().minusDays(7),
                                LocalTime.now(),
                                LocalTime.now().plusMinutes(30),
                                RequestCode("ATTC"),
                                RequestCode("OFFICE1"),
                                RequestCode("STAFF01"),
                                RequestCode("TEAM01"),
                                "Some notes about the appointment",
                                true,
                            )
                        )
                    )
                )
            )
            .andExpect(status().isCreated)

        val appointment = contactRepository.findByExternalReference("${Contact.REFERENCE_PREFIX}$appointmentReference")
        assertThat(appointment).isNotNull
        with(appointment!!) {
            assertThat(date).isEqualTo(LocalDate.now().minusDays(7))
            assertThat(sensitive).isTrue
            assertThat(notes).isEqualTo("Some notes about the appointment")
            assertThat(location?.code).isEqualTo("OFFICE1")
            assertThat(team.code).isEqualTo("TEAM01")
            assertThat(staff.code).isEqualTo("STAFF01")
            assertThat(requirement?.id).isEqualTo(REQUIREMENTS[2].id)
        }
    }

    @Test
    fun `can delete an appointment`() {
        val existing = contactRepository.findAll().first { it.externalReference != null  }
        val appointmentReference = UUID.fromString(existing.externalReference!!.takeLast(36))

        mockMvc
            .perform(
                delete("/appointments").withToken().withJson(
                    DeleteAppointmentsRequest(listOf(AppointmentReference(appointmentReference)))
                )
            )
            .andExpect(status().isNoContent)

        val appointment = contactRepository.findByExternalReference("${Contact.REFERENCE_PREFIX}$appointmentReference")
        assertThat(appointment).isNull()
    }
}
