package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.repository.findByIdOrNull
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
import uk.gov.justice.digital.hmpps.data.TestData.INVALID_LICENCE_CONDITION
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
        val requirementIds = REQUIREMENTS.map { it.id }
        val licenceConditionIds = LICENCE_CONDITIONS.map { it.id }
        val fromDate = LocalDate.of(2030, 1, 1)
        val toDate = LocalDate.of(2030, 12, 31)

        val response = mockMvc.post("/appointments/search") {
            withToken()
            json = GetAppointmentsRequest(
                requirementIds = requirementIds,
                licenceConditionIds = licenceConditionIds,
                fromDate = fromDate,
                toDate = toDate,
            )
        }.andExpect { status { isOk() } }.andReturn().response.contentAsJson<GetAppointmentsResponse>()

        val appointments = response.content.getValue("A000001")
        assertThat(appointments).isNotEmpty
        assertThat(appointments).allSatisfy { appointment ->
            assertThat(appointment.date).isBetween(fromDate, toDate)
            assertThat(listOfNotNull(appointment.requirementId, appointment.licenceConditionId)).hasSize(1)
        }
        assertThat(appointments.mapNotNull { it.requirementId }).allMatch(requirementIds::contains)
        assertThat(appointments.mapNotNull { it.licenceConditionId }).allMatch(licenceConditionIds::contains)

        with(appointments.first()) {
            val expected = contactRepository.findByIdOrNull(id)!!
            assertThat(reference).isEqualTo(expected.externalReference?.takeLast(36))
            assertThat(crn).isEqualTo(expected.person.crn)
            assertThat(listOfNotNull(requirementId, licenceConditionId)).hasSize(1)
            assertThat(requirementId).isEqualTo(expected.requirement?.id)
            assertThat(licenceConditionId).isEqualTo(expected.licenceCondition?.id)
            assertThat(date).isEqualTo(expected.date)
            assertThat(startTime).isEqualTo(expected.startTime)
            assertThat(endTime).isEqualTo(expected.endTime)
            assertThat(createdAt).isEqualTo(expected.createdDatetime)
            assertThat(updatedAt).isEqualTo(expected.lastUpdatedDatetime)
            assertThat(type.code).isEqualTo(expected.type.code)
            assertThat(type.description).isEqualTo(expected.type.description)
            assertThat(outcome?.code).isEqualTo(expected.outcome?.code)
            assertThat(outcome?.description).isEqualTo(expected.outcome?.description)
            assertThat(location?.code).isEqualTo(expected.location?.code)
            assertThat(location?.description).isEqualTo(expected.location?.description)
            assertThat(staff.code).isEqualTo(expected.staff.code)
            assertThat(staff.name.forename).isEqualTo(expected.staff.forename)
            assertThat(staff.name.surname).isEqualTo(expected.staff.surname)
            assertThat(team.code).isEqualTo(expected.team.code)
            assertThat(team.description).isEqualTo(expected.team.description)
            assertThat(notes).isEqualTo(expected.notes)
            assertThat(sensitive).isEqualTo(expected.sensitive)
        }
    }

    @Test
    fun `get appointments without start date`() {
        mockMvc.post("/appointments/search") {
            withToken()
            json = GetAppointmentsRequest(
                requirementIds = REQUIREMENTS.map { it.id },
                licenceConditionIds = LICENCE_CONDITIONS.map { it.id },
                fromDate = null,
                toDate = LocalDate.of(2030, 1, 1),
            )
        }.andExpect {
            status { isOk() }
            jsonPath("content.A000001[*].date", everyItem(lessThanOrEqualTo("2030-01-01")))
        }
    }

    @Test
    fun `get appointments without end date`() {
        mockMvc.post("/appointments/search") {
            withToken()
            json = GetAppointmentsRequest(
                requirementIds = REQUIREMENTS.map { it.id },
                licenceConditionIds = LICENCE_CONDITIONS.map { it.id },
                fromDate = LocalDate.of(2030, 1, 2),
                toDate = null,
            )
        }.andExpect {
            status { isOk() }
            jsonPath("content.A000001[*].date", everyItem(greaterThanOrEqualTo("2030-01-02")))
        }
    }

    @Test
    fun `can create an appointment - contact outcome not found`() {
        mockMvc
            .post("/appointments") {
                withToken()
                json = createAppointment().copy(outcome = RequestCode("UNKNOWN")).asList()
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid AppointmentOutcome codes: [UNKNOWN]")
            }
    }

    @Test
    fun `can create an appointment - location not found`() {
        mockMvc
            .post("/appointments") {
                withToken()
                json = createAppointment().copy(location = RequestCode("OFFICE99")).asList()
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid OfficeLocation codes: [OFFICE99]")
            }
    }

    @Test
    fun `can create an appointment - staff not found`() {
        mockMvc
            .post("/appointments") {
                withToken()
                json = createAppointment().copy(staff = RequestCode("STAFF99")).asList()
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid Staff codes: [STAFF99]")
            }
    }

    @Test
    fun `can create an appointment - team not found`() {
        mockMvc
            .post("/appointments") {
                withToken()
                json = createAppointment().copy(team = RequestCode("TEAM99")).asList()
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid Team codes: [TEAM99]")
            }
    }

    @Test
    fun `can not create appointment without one of requirement or licence condition id`() {
        assertThatThrownBy { createAppointment().copy(requirementId = null) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Either licence condition or requirement id must be specified.")
    }

    @Test
    fun `can only create appointments against accredited programmes requirements`() {
        mockMvc
            .post("/appointments") {
                withToken()
                json = createAppointment().copy(
                    requirementId = null,
                    licenceConditionId = INVALID_LICENCE_CONDITION.id,
                ).asList()
            }
            .andExpect { status { isBadRequest() } }
            .andReturn().response.contentAsJson<ErrorResponse>().also {
                assertThat(it.message).isEqualTo("Invalid LicenceCondition IDs: [${INVALID_LICENCE_CONDITION.id}]")
            }
    }

    @Test
    fun `can create appointments`() {
        val requests = (List(3) { createAppointment() } + List(3) {
            createAppointment().copy(
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
            assertThat(description).isEqualTo("Description")
        }
    }

    @ParameterizedTest
    @EnumSource(CreateAppointmentRequest.Type::class)
    fun `can create an appointment`(type: CreateAppointmentRequest.Type) {
        val request = createAppointment().copy(
            notes = "Some notes about the appointment and type",
            type = type,
        )
        mockMvc.post("/appointments") {
            withToken()
            json = request.asList()
        }.andExpect { status { isCreated() } }

        val appointment =
            contactRepository.findByExternalReference("${Contact.REFERENCE_PREFIX}${request.reference}")
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
    fun `can create appointment in the past without an outcome`() {
        val request = createAppointment().copy(
            date = LocalDate.now().minusDays(7),
            outcome = null
        )

        mockMvc.post("/appointments") {
            withToken()
            json = request.asList()
        }.andExpect { status { isCreated() } }

        val appointment =
            contactRepository.findByExternalReference("${Contact.REFERENCE_PREFIX}${request.reference}")
        assertThat(appointment).isNotNull
        with(appointment!!) {
            assertThat(date).isEqualTo(LocalDate.now().minusDays(7))
            assertThat(outcome).isNull()
        }
    }

    @Test
    fun `can update an appointment`() {
        val existing = givenExistingContact()

        mockMvc.put("/appointments") {
            withToken()
            json = existing.toUpdate().copy(
                date = LocalDate.now().plusDays(1),
                startTime = LocalTime.now(),
                endTime = LocalTime.now().plusMinutes(30),
                sensitive = true,
                outcome = RequestCode("AA"),
                location = RequestCode("OFFICE1"),
                notes = "Some appended notes",
                description = "Updated Description"
            ).asList()
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
            assertThat(description).isEqualTo("Updated Description")
        }
    }

    @Test
    fun `can reschedule an appointment with an outcome`() {
        val existing = TestData.RESCHEDULABLE_APPOINTMENT
        val updatedStart = existing.startTime!!.plusMinutes(15)
        val updatedEnd = existing.endTime!!.plusMinutes(15)

        mockMvc.put("/appointments") {
            withToken()
            json = existing.toUpdate().copy(
                date = updatedStart.toLocalDate(),
                startTime = updatedStart.toLocalTime(),
                endTime = updatedEnd.toLocalTime(),
            ).asList()
        }.andExpect { status { isNoContent() } }

        val appointment = contactRepository.findByExternalReference(existing.externalReference!!)!!
        assertThat(appointment.startTime).isEqualTo(updatedStart)
        assertThat(appointment.endTime).isEqualTo(updatedEnd)
        assertThat(appointment.outcome?.code).isEqualTo("ATTC")
    }

    @Test
    fun `can change an existing outcome`() {
        val existing = TestData.OUTCOME_CHANGEABLE_APPOINTMENT

        mockMvc.put("/appointments") {
            withToken()
            json = existing.toUpdate().copy(
                outcome = RequestCode("FTC"),
            ).asList()
        }.andExpect { status { isNoContent() } }

        val appointment = contactRepository.findByExternalReference(existing.externalReference!!)!!
        assertThat(appointment.outcome?.code).isEqualTo("FTC")
        assertThat(appointment.event?.ftcCount).isEqualTo(1)
        assertThat(appointment.enforcement).isTrue
        assertThat(enforcementRepository.findAll().filter { it.contact.id == appointment.id })
            .singleElement().matches { it.action?.code == "ROM" }
    }

    @Test
    fun `logging a non-complied outcome increments failure to comply count`() {
        val existing1 = givenExistingContact(date = LocalDate.now().minusDays(1))
        val existing2 = givenExistingContact(date = LocalDate.now().minusDays(2))

        listOf(existing1, existing2).forEach { existing ->
            mockMvc.put("/appointments") {
                withToken()
                json = existing.toUpdate().copy(
                    sensitive = true,
                    outcome = RequestCode("FTC"),
                    location = RequestCode("OFFICE1"),
                    notes = "Some appended notes",
                    description = "Description"
                ).asList()
            }.andExpect { status { isNoContent() } }
        }

        val appointment = contactRepository.findByExternalReference(existing1.externalReference!!)!!
        assertThat(appointment.attended).isFalse
        assertThat(appointment.complied).isFalse
        assertThat(appointment.outcome?.code).isEqualTo("FTC")
        assertThat(appointment.event?.ftcCount).isEqualTo(2)
        assertThat(appointment.enforcement).isTrue
        assertThat(appointment.notes).matches(
            """
            Some notes
            
            \d{2}/\d{2}/\d{4} \d{2}:\d{2}
            Enforcement Action: Refer to manager

            Some appended notes
            """.trimIndent()
        )
        val enforcementAction = enforcementActionRepository.findByIdOrNull(appointment.enforcementActionId!!)!!
        assertThat(enforcementAction.code).isEqualTo("ROM")
        val enforcement = enforcementRepository.findAll().single { it.contact.id == appointment.id }
        assertThat(enforcement.action?.id).isEqualTo(enforcementAction.id)
        assertThat(enforcement.responseDate?.toLocalDate()).isEqualTo(LocalDate.now().plusDays(7))
        val enforcementContacts = contactRepository.findAll()
            .filter { it.person.crn == appointment.person.crn && it.type.code == enforcementAction.contactType.code }
        assertThat(enforcementContacts).hasSize(2)
        val enforcementReviews =
            contactRepository.findAll().filter { it.person.crn == appointment.person.crn && it.type.code == "ARWS" }
        assertThat(enforcementReviews).hasSize(1)
    }

    @Test
    fun `can delete an appointment`() {
        val existing = givenExistingContact()

        mockMvc.delete("/appointments") {
            withToken()
            json = DeleteAppointmentsRequest(listOf(AppointmentReference(existing.toUpdate().reference)))
        }.andExpect { status { isNoContent() } }

        val appointment = contactRepository.findByExternalReference(existing.externalReference!!)
        assertThat(appointment).isNull()
    }

    private fun givenExistingContact(date: LocalDate = LocalDate.now().plusDays(7)) = contactRepository.save(
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

    fun createAppointment(): CreateAppointmentRequest {
        val startTime = LocalTime.now()
        return CreateAppointmentRequest(
            reference = UUID.randomUUID(),
            requirementId = REQUIREMENTS[2].id,
            licenceConditionId = null,
            date = LocalDate.now().minusDays(7),
            startTime = startTime,
            endTime = startTime.plusMinutes(30),
            outcome = RequestCode("ATTC"),
            location = RequestCode("OFFICE1"),
            staff = RequestCode("STAFF01"),
            team = RequestCode("TEAM01"),
            notes = "Some notes about the appointment",
            sensitive = true,
            description = "Description",
        )
    }

    fun CreateAppointmentRequest.asList() = CreateAppointmentsRequest(listOf(this))

    fun Contact.toUpdate() = UpdateAppointmentRequest(
        reference = UUID.fromString(externalReference!!.takeLast(36)),
        date = date,
        startTime = startTime!!.toLocalTime(),
        endTime = endTime!!.toLocalTime(),
        sensitive = false,
        outcome = outcome?.let { RequestCode(it.code) },
        location = location?.let { RequestCode(it.code) },
        team = RequestCode(team.code),
        staff = RequestCode(staff.code),
        notes = notes,
        description = description,
    )

    fun UpdateAppointmentRequest.asList() = UpdateAppointmentsRequest(listOf(this))
}
