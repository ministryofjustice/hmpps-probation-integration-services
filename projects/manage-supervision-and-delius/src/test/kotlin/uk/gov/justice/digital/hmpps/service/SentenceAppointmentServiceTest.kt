package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.User
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.client.BankHolidayClient
import uk.gov.justice.digital.hmpps.data.generator.AppointmentGenerator.PERSON_APPOINTMENT
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.compliance.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@ExtendWith(MockitoExtension::class)
class SentenceAppointmentServiceTest {

    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    lateinit var sentenceAppointmentRepository: SentenceAppointmentRepository

    @Mock
    lateinit var appointmentTypeRepository: AppointmentTypeRepository

    @Mock
    lateinit var offenderManagerRepository: OffenderManagerRepository

    @Mock
    lateinit var eventSentenceRepository: EventSentenceRepository

    @Mock
    lateinit var requirementRepository: RequirementRepository

    @Mock
    lateinit var licenceConditionRepository: LicenceConditionRepository

    @Mock
    lateinit var staffUserRepository: StaffUserRepository

    @Mock
    lateinit var locationRepository: LocationRepository

    @Mock
    lateinit var nsiRepository: NsiRepository

    @Mock
    lateinit var bankHolidayClient: BankHolidayClient

    @Mock
    lateinit var userService: UserService

    @Mock
    lateinit var objectMapper: ObjectMapper

    @InjectMocks
    lateinit var service: SentenceAppointmentService

    private val uuid: UUID = UUID.randomUUID()

    private val user = User("user", OffenderManagerGenerator.TEAM.code)

    @Test
    fun `licence and requirement id provided`() {
        val appointment = CreateAppointment(
            user,
            type = CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
            start = ZonedDateTime.now().plusDays(1),
            end = ZonedDateTime.now().plusDays(2),
            interval = CreateAppointment.Interval.WEEK,
            numberOfAppointments = 3,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = uuid,
            requirementId = 2,
            licenceConditionId = 3
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        val exception = assertThrows<InvalidRequestException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(
            exception.message,
            equalTo("Either licence id or requirement id or nsi id can be provided")
        )

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoInteractions(eventSentenceRepository)
        verifyNoInteractions(licenceConditionRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(sentenceAppointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `start date before end date`() {
        val appointment = CreateAppointment(
            user,
            type = CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
            start = ZonedDateTime.now().plusDays(2),
            end = ZonedDateTime.now().plusDays(1),
            interval = CreateAppointment.Interval.FORTNIGHT,
            numberOfAppointments = 3,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = uuid
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        val exception = assertThrows<InvalidRequestException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("Appointment end time cannot be before start time"))

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoInteractions(eventSentenceRepository)
        verifyNoInteractions(licenceConditionRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(sentenceAppointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `until before start date`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
            start = ZonedDateTime.now().plusDays(2),
            end = ZonedDateTime.now().plusDays(2),
            until = ZonedDateTime.now().plusDays(1),
            interval = CreateAppointment.Interval.FORTNIGHT,
            numberOfAppointments = 3,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = uuid
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )

        val exception = assertThrows<InvalidRequestException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("Until cannot be before start time"))

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoInteractions(eventSentenceRepository)
        verifyNoInteractions(licenceConditionRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(sentenceAppointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `event not found`() {
        val appointment = CreateAppointment(
            user,
            type = CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
            start = ZonedDateTime.now().plusDays(1),
            end = ZonedDateTime.now().plusDays(1),
            interval = CreateAppointment.Interval.FOUR_WEEKS,
            numberOfAppointments = 1,
            eventId = 1,
            uuid = uuid
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(eventSentenceRepository.existsById(appointment.eventId!!)).thenReturn(false)
        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("Event with eventId of 1 not found"))

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoMoreInteractions(eventSentenceRepository)
        verifyNoInteractions(licenceConditionRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(sentenceAppointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `requirement not found`() {
        val appointment = CreateAppointment(
            user,
            type = CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
            start = ZonedDateTime.now().plusDays(1),
            end = ZonedDateTime.now().plusDays(2),
            interval = CreateAppointment.Interval.DAY,
            numberOfAppointments = 1,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = uuid,
            requirementId = 2
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(eventSentenceRepository.existsById(appointment.eventId!!)).thenReturn(true)
        whenever(requirementRepository.existsById(appointment.requirementId!!)).thenReturn(false)
        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("Requirement with requirementId of 2 not found"))

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoMoreInteractions(eventSentenceRepository)
        verifyNoMoreInteractions(requirementRepository)
        verifyNoInteractions(licenceConditionRepository)
        verifyNoInteractions(sentenceAppointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `licence not found`() {
        val appointment = CreateAppointment(
            user,
            type = CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
            start = ZonedDateTime.now().plusDays(1),
            end = ZonedDateTime.now().plusDays(2),
            interval = CreateAppointment.Interval.DAY,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = uuid,
            licenceConditionId = 3
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(eventSentenceRepository.existsById(appointment.eventId!!)).thenReturn(true)
        whenever(licenceConditionRepository.existsById(appointment.licenceConditionId!!)).thenReturn(false)
        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("LicenceCondition with licenceConditionId of 3 not found"))

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoMoreInteractions(eventSentenceRepository)
        verifyNoMoreInteractions(licenceConditionRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(sentenceAppointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `nsi not found`() {
        val appointment = CreateAppointment(
            user,
            type = CreateAppointment.Type.InitialAppointmentInOfficeNS.code,
            start = ZonedDateTime.now().plusDays(1),
            end = ZonedDateTime.now().plusDays(2),
            interval = CreateAppointment.Interval.DAY,
            uuid = uuid,
            nsiId = 3
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(nsiRepository.existsById(appointment.nsiId!!)).thenReturn(false)

        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("Nsi with nsiId of 3 not found"))

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoMoreInteractions(eventSentenceRepository)
        verifyNoMoreInteractions(licenceConditionRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(sentenceAppointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `error overlapping appointment`() {
        val appointment = CreateAppointment(
            user,
            type = CreateAppointment.Type.HomeVisitToCaseNS.code,
            start = ZonedDateTime.now().plusDays(1),
            end = ZonedDateTime.now().plusDays(2),
            numberOfAppointments = 3,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = uuid
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(staffUserRepository.findUserAndTeamAssociation(appointment.user.username, appointment.user.teamCode))
            .thenReturn(UserTeamInfo(1, 2, 3, 4))

        whenever(eventSentenceRepository.existsById(appointment.eventId!!)).thenReturn(true)

        whenever(appointmentTypeRepository.findByCode(appointment.type)).thenReturn(
            ContactType(
                1,
                appointment.type,
                true,
                "description",
                locationRequired = "N",
                editable = true
            )
        )

        whenever(
            sentenceAppointmentRepository.getClashCount(
                OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE.person.id,
                appointment.start.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                appointment.start.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault())),
                appointment.end.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault()))
            )
        ).thenReturn(1)

        assertThrows<ConflictException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }
    }

    @Test
    fun `non personal level contact without appointment ids`() {
        val appointment = CreateAppointment(
            user,
            type = CreateAppointment.Type.HomeVisitToCaseNS.code,
            start = ZonedDateTime.now().plusDays(1),
            end = ZonedDateTime.now().plusDays(2),
            interval = CreateAppointment.Interval.WEEK,
            numberOfAppointments = 3,
            uuid = uuid,
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )

        whenever(appointmentTypeRepository.findByCode(appointment.type)).thenReturn(
            ContactType(
                1,
                appointment.type,
                true,
                "description",
                locationRequired = "N",
                editable = true
            )
        )

        val exception = assertThrows<InvalidRequestException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(
            exception.message,
            equalTo("Event id, licence id, requirement id or nsi id need to be provided for contact type ${CreateAppointment.Type.HomeVisitToCaseNS.code}")
        )
    }

    @Test
    fun `duplicate uuid submitted`() {
        val appointment = CreateAppointment(
            user,
            type = CreateAppointment.Type.HomeVisitToCaseNS.code,
            start = ZonedDateTime.now().plusDays(1),
            end = ZonedDateTime.now().plusDays(2),
            interval = CreateAppointment.Interval.WEEK,
            numberOfAppointments = 1,
            uuid = UUID.fromString("7d26b633-f77c-4fcb-a37d-30f19a3be9f2"),
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )

        whenever(appointmentTypeRepository.findByCode(appointment.type)).thenReturn(
            ContactType(
                1,
                appointment.type,
                true,
                "description",
                offenderContact = true,
                locationRequired = "N",
                editable = true
            )
        )

        whenever(sentenceAppointmentRepository.findByExternalReference(appointment.urn)).thenReturn(PERSON_APPOINTMENT)

        val exception = assertThrows<ConflictException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(
            exception.message,
            equalTo("Duplicate external reference urn:uk:gov:hmpps:manage-supervision-service:appointment:${appointment.uuid}")
        )
    }

    @ParameterizedTest
    @MethodSource("createAppointment")
    fun `requirement or licence without event`(appointment: CreateAppointment) {
        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )

        val exception = assertThrows<InvalidRequestException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(
            exception.message,
            equalTo("Event id required when licence id or requirement id provided")
        )
    }

    data class UserTeamInfo(
        val _userId: Long,
        val _staffId: Long,
        val _teamId: Long,
        val _providerId: Long
    ) : UserTeam {
        override val userId: Long
            get() = _userId
        override val staffId: Long
            get() = _staffId
        override val teamId: Long
            get() = _teamId
        override val providerId: Long
            get() = _providerId
    }

    companion object {
        val user = User("user", OffenderManagerGenerator.TEAM.code)

        @JvmStatic
        fun createAppointment() = listOf(
            CreateAppointment(
                user,
                type = CreateAppointment.Type.PlannedOfficeVisitNS.code,
                start = ZonedDateTime.now().plusDays(1),
                end = ZonedDateTime.now().plusDays(1).plusHours(1),
                licenceConditionId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            ),
            CreateAppointment(
                user,
                type = CreateAppointment.Type.PlannedOfficeVisitNS.code,
                start = ZonedDateTime.now().plusDays(1),
                end = ZonedDateTime.now().plusDays(1).plusHours(1),
                licenceConditionId = PersonGenerator.EVENT_1.id,
                uuid = UUID.randomUUID()
            )
        )
    }
}