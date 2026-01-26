package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.advice.ErrorResponse
import uk.gov.justice.digital.hmpps.data.EnforcementActionRepository
import uk.gov.justice.digital.hmpps.data.EnforcementRepository
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.data.TestData.CA_COMMUNITY_EVENT
import uk.gov.justice.digital.hmpps.data.TestData.CA_PERSON
import uk.gov.justice.digital.hmpps.data.TestData.LICENCE_CONDITIONS
import uk.gov.justice.digital.hmpps.data.TestData.REQUIREMENTS
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator.toCrn
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.contact.Contact
import uk.gov.justice.digital.hmpps.model.*
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.RequirementRepository
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.contentAsJson
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.json
import uk.gov.justice.digital.hmpps.test.MockMvcExtensions.withToken
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
internal class AppointmentControllerIntegrationTest @Autowired constructor(
    private val mockMvc: MockMvc,
    private val contactRepository: ContactRepository,
    private val enforcementActionRepository: EnforcementActionRepository,
    private val enforcementRepository: EnforcementRepository,
    private val requirementRepository: RequirementRepository,
) {
    @Test
    fun `exception when end date before start date`() {
        mockMvc.post("/appointments/search") {
            withToken()
            json = GetAppointmentsRequest(
                requirementIds = listOf(1L),
                licenceConditionIds = listOf(2L),
                fromDate = LocalDate.of(2030, 12, 31),
                toDate = LocalDate.of(2030, 12, 1),
            )
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `exception when no requirement or licence condition ids provided`() {
        mockMvc.post("/appointments/search") {
            withToken()
            json = GetAppointmentsRequest(
                requirementIds = emptyList(),
                licenceConditionIds = emptyList(),
                fromDate = LocalDate.of(2030, 1, 1),
                toDate = LocalDate.of(2030, 12, 31),
            )
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `exception when providing over 500 requirement ids`() {
        mockMvc.post("/appointments/search") {
            withToken()
            json = GetAppointmentsRequest(
                requirementIds = (1..1001).map { it.toLong() },
                licenceConditionIds = emptyList(),
                fromDate = LocalDate.of(2030, 1, 1),
                toDate = LocalDate.of(2030, 12, 31),
            )
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `exception when providing over 500 licence condition ids`() {
        mockMvc.post("/appointments/search") {
            withToken()
            json = GetAppointmentsRequest(
                requirementIds = emptyList(),
                licenceConditionIds = (1..1001).map { it.toLong() },
                fromDate = LocalDate.of(2030, 1, 1),
                toDate = LocalDate.of(2030, 12, 31),
            )
        }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `get appointments success`() {
        mockMvc.post("/appointments/search") {
            withToken()
            json = GetAppointmentsRequest(
                requirementIds = TestData.REQUIREMENTS.map { it.id },
                licenceConditionIds = TestData.LICENCE_CONDITIONS.map { it.id },
                fromDate = LocalDate.of(2030, 1, 1),
                toDate = LocalDate.of(2030, 12, 31),
            )
        }.andExpect {
            status { isOk() }
            content {
                json(
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
                    """.trimIndent(), JsonCompareMode.STRICT
                )
            }
        }
    }

    @Test
    fun `can create an appointment - contact outcome not found`() {
        val appointmentReference = UUID.randomUUID()
        mockMvc
            .post("/appointments") {
                withToken()
                json = CreateAppointmentsRequest(
                    listOf(
                        CreateAppointmentRequest(
                            appointmentReference,
                            REQUIREMENTS[2].id,
                            null,
                            LocalDate.now().minusDays(7),
                            LocalTime.now(),
                            LocalTime.now().plusMinutes(30),
                            RequestCode("UNKNOWN"),
                            RequestCode("OFFICE1"),
                            RequestCode("STAFF01"),
                            RequestCode("TEAM01"),
                            "Some notes about the appointment",
                            true,
                        )
                    )
                )
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid AppointmentOutcome codes: [UNKNOWN]")
            }
    }

    @Test
    fun `can create an appointment - location not found`() {
        val appointmentReference = UUID.randomUUID()
        mockMvc
            .post("/appointments") {
                withToken()
                json = CreateAppointmentsRequest(
                    listOf(
                        CreateAppointmentRequest(
                            appointmentReference,
                            REQUIREMENTS[2].id,
                            null,
                            LocalDate.now().minusDays(7),
                            LocalTime.now(),
                            LocalTime.now().plusMinutes(30),
                            RequestCode("ATTC"),
                            RequestCode("OFFICE99"),
                            RequestCode("STAFF01"),
                            RequestCode("TEAM01"),
                            "Some notes about the appointment",
                            true,
                        )
                    )
                )
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid OfficeLocation codes: [OFFICE99]")
            }
    }

    @Test
    fun `can create an appointment - staff not found`() {
        val appointmentReference = UUID.randomUUID()
        mockMvc
            .post("/appointments") {
                withToken()
                json = CreateAppointmentsRequest(
                    listOf(
                        CreateAppointmentRequest(
                            reference = appointmentReference,
                            requirementId = REQUIREMENTS[2].id,
                            licenceConditionId = null,
                            date = LocalDate.now().minusDays(7),
                            startTime = LocalTime.now(),
                            endTime = LocalTime.now().plusMinutes(30),
                            outcome = RequestCode("ATTC"),
                            location = RequestCode("OFFICE1"),
                            staff = RequestCode("STAFF99"),
                            team = RequestCode("TEAM01"),
                            notes = "Some notes about the appointment",
                            sensitive = true,
                        )
                    )

                )
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid Staff codes: [STAFF99]")
            }
    }

    @Test
    fun `can create an appointment - team not found`() {
        val appointmentReference = UUID.randomUUID()
        mockMvc
            .post("/appointments") {
                withToken()
                json = CreateAppointmentsRequest(
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
                            RequestCode("TEAM99"),
                            "Some notes about the appointment",
                            true,
                        )
                    )
                )
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid Team codes: [TEAM99]")
            }
    }

    @Test
    fun `can not create appointment without one of requirement or licence condition id`() {
        assertThatThrownBy {
            CreateAppointmentRequest(
                UUID.randomUUID(),
                null,
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
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Either licence condition or requirement id must be specified.")
    }

    @Test
    fun `can create appointments`() {
        val request = CreateAppointmentRequest(
            reference = UUID.randomUUID(),
            requirementId = REQUIREMENTS[2].id,
            licenceConditionId = null,
            date = LocalDate.now().minusDays(7),
            startTime = LocalTime.now(),
            endTime = LocalTime.now().plusMinutes(30),
            outcome = RequestCode("ATTC"),
            location = RequestCode("OFFICE1"),
            staff = RequestCode("STAFF01"),
            team = RequestCode("TEAM01"),
            notes = "Some notes about the appointment",
            sensitive = true,
        )
        val requests = (List(3) { request } + List(3) {
            request.copy(
                requirementId = null,
                licenceConditionId = LICENCE_CONDITIONS[1].id
            )
        }).map { it.copy(reference = UUID.randomUUID()) }

        mockMvc.post("/appointments") {
            withToken()
            json = CreateAppointmentsRequest(requests)
        }.andExpect { status { isCreated() } }

        val appointment =
            contactRepository.findByExternalReference("${Contact.REFERENCE_PREFIX}${requests[0].reference}")
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

    @ParameterizedTest
    @EnumSource(CreateAppointmentRequest.Type::class)
    fun `can create an appointment`(type: CreateAppointmentRequest.Type) {
        val appointmentReference = UUID.randomUUID()
        mockMvc.post("/appointments") {
            withToken()
            json = CreateAppointmentsRequest(
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
                        "Some notes about the appointment and type",
                        true,
                        type = type
                    )
                )
            )
        }.andExpect { status { isCreated() } }

        val appointment =
            contactRepository.findByExternalReference("${Contact.REFERENCE_PREFIX}$appointmentReference")
        assertThat(appointment).isNotNull
        with(appointment!!) {
            assertThat(date).isEqualTo(LocalDate.now().minusDays(7))
            assertThat(sensitive).isTrue
            assertThat(notes).isEqualTo("Some notes about the appointment and type")
            assertThat(this.type.code).isEqualTo(type.code)
        }

        if (type == CreateAppointmentRequest.Type.PRE_GROUP_ONE_TO_ONE_MEETING) {
            val requirement = requirementRepository.findByIdOrNull(REQUIREMENTS[2].id)
            assertThat(requirement!!.commencementDate).isEqualTo(
                LocalDate.now().minusDays(7).atStartOfDay(EuropeLondon)
            )

            val ecomContact = contactRepository.findByRequirementIdAndTypeCode(REQUIREMENTS[2].id, "ECOM")
            assertThat(ecomContact).isNotNull
        }
    }

    @Test
    fun `can update an appointment`() {
        val (existing, appointmentReference) = givenExistingContact()

        mockMvc.put("/appointments") {
            withToken()
            json = UpdateAppointmentsRequest(
                listOf(
                    UpdateAppointmentRequest(
                        reference = appointmentReference,
                        date = LocalDate.now().plusDays(1),
                        startTime = LocalTime.now(),
                        endTime = LocalTime.now().plusMinutes(30),
                        sensitive = true,
                        outcome = RequestCode("AA"),
                        location = RequestCode("OFFICE1"),
                        team = RequestCode("TEAM01"),
                        staff = RequestCode("STAFF01"),
                        notes = "Some appended notes"
                    )
                )
            )
        }.andExpect { status { isNoContent() } }

        val appointment = contactRepository.findByExternalReference(existing.externalReference!!)
        assertThat(appointment).isNotNull
        with(appointment!!) {
            assertThat(notes).isEqualTo(
                """
                |Some notes
                |
                |Some appended notes
                """.trimMargin()
            )
            assertThat(outcome?.code).isEqualTo("AA")
            assertThat(date).isEqualTo(LocalDate.now().plusDays(1))
            assertThat(sensitive).isTrue
        }
    }

    @Test
    fun `logging a non-complied outcome increments failure to comply count`() {
        val (existing1, appointmentReference1) = givenExistingContact(date = LocalDate.now().minusDays(1))
        val (existing2, appointmentReference2) = givenExistingContact(date = LocalDate.now().minusDays(2))

        listOf(
            existing1 to appointmentReference1,
            existing2 to appointmentReference2
        ).forEach { (existing, reference) ->
            mockMvc.put("/appointments") {
                withToken()
                json = UpdateAppointmentsRequest(
                    listOf(
                        UpdateAppointmentRequest(
                            reference = reference,
                            date = existing.date,
                            startTime = existing.startTime!!.toLocalTime(),
                            endTime = existing.endTime!!.toLocalTime(),
                            sensitive = true,
                            outcome = RequestCode("FTC"),
                            location = RequestCode("OFFICE1"),
                            team = RequestCode("TEAM01"),
                            staff = RequestCode("STAFF01"),
                            notes = "Some appended notes"
                        )
                    )
                )
            }.andExpect { status { isNoContent() } }
        }

        val appointment = contactRepository.findByExternalReference(existing1.externalReference!!)!!
        assertThat(appointment.attended).isFalse
        assertThat(appointment.complied).isFalse
        assertThat(appointment.outcome?.code).isEqualTo("FTC")
        assertThat(appointment.event?.ftcCount).isEqualTo(2)
        assertThat(appointment.enforcement).isTrue
        val enforcementAction = enforcementActionRepository.findByIdOrNull(appointment.enforcementActionId!!)!!
        assertThat(enforcementAction.code).isEqualTo("ROM")
        val enforcement = enforcementRepository.findAll().single { it.contact.id == appointment.id }
        assertThat(enforcement.action?.id).isEqualTo(enforcementAction.id)
        assertThat(enforcement.responseDate?.toLocalDate()).isEqualTo(LocalDate.now().plusDays(7))
        val enforcementContacts = contactRepository.findAll()
            .filter { it.person.crn == appointment.person.crn && it.type.code == enforcementAction.contactType.code }
        assertThat(enforcementContacts).hasSize(2)
        assertThat(enforcementContacts[0].notes).matches(
            """
            Some notes
            
            \d{2}/\d{2}/\d{4} \d{2}:\d{2}
            Enforcement Action: Refer to manager
            """.trimIndent()
        )
        val enforcementReviews =
            contactRepository.findAll().filter { it.person.crn == appointment.person.crn && it.type.code == "ARWS" }
        assertThat(enforcementReviews).hasSize(1)
    }

    @Test
    fun `can delete an appointment`() {
        val (existing, appointmentReference) = givenExistingContact()

        mockMvc.delete("/appointments") {
            withToken()
            json = DeleteAppointmentsRequest(listOf(AppointmentReference(appointmentReference)))
        }.andExpect { status { isNoContent() } }

        val appointment = contactRepository.findByExternalReference(existing.externalReference!!)
        assertThat(appointment).isNull()
    }

    private fun givenExistingContact(date: LocalDate = LocalDate.now().plusDays(7)): Pair<Contact, UUID> {
        val existing = contactRepository.save(
            Contact(
                id = id(),
                person = CA_PERSON.toCrn(),
                event = CA_COMMUNITY_EVENT,
                date = date,
                startTime = ZonedDateTime.now().plusDays(7),
                endTime = ZonedDateTime.now().plusDays(7).plusMinutes(30),
                type = TestData.APPOINTMENT_CONTACT_TYPE,
                staff = TestData.STAFF,
                team = TestData.TEAM,
                provider = TestData.PROVIDER,
                notes = "Some notes",
                externalReference = "${Contact.REFERENCE_PREFIX}${UUID.randomUUID()}",
                sensitive = false,
            )
        )
        val appointmentReference = UUID.fromString(existing.externalReference!!.takeLast(36))
        return Pair(existing, appointmentReference)
    }
}
