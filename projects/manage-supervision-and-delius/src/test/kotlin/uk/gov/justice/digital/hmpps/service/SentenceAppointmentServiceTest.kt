package uk.gov.justice.digital.hmpps.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.api.model.appointment.User
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
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
    lateinit var appointmentRepository: AppointmentRepository

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
    lateinit var objectMapper: ObjectMapper

    @InjectMocks
    lateinit var service: SentenceAppointmentService

    private val uuid: UUID = UUID.randomUUID()

    private val user = User("user", "team")

    @Test
    fun `licence and requirement id provided`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(2),
            interval = CreateAppointment.Interval.WEEK,
            numberOfAppointments = 3,
            PersonGenerator.EVENT_1.id,
            uuid,
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
            equalTo("Either licence id or requirement id can be provided, not both")
        )

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoInteractions(eventSentenceRepository)
        verifyNoInteractions(licenceConditionRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(appointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `start date before end date`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
            start = ZonedDateTime.now().plusDays(2),
            end = ZonedDateTime.now().plusDays(1),
            interval = CreateAppointment.Interval.FORTNIGHT,
            numberOfAppointments = 3,
            PersonGenerator.EVENT_1.id,
            uuid
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
        verifyNoInteractions(appointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `until before start date`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
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
        verifyNoInteractions(appointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `event not found`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(1),
            interval = CreateAppointment.Interval.FOUR_WEEKS,
            numberOfAppointments = 1,
            1,
            uuid
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(eventSentenceRepository.existsById(appointment.eventId)).thenReturn(false)
        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("Event with eventId of 1 not found"))

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoMoreInteractions(eventSentenceRepository)
        verifyNoInteractions(licenceConditionRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(appointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `requirement not found`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(2),
            interval = CreateAppointment.Interval.DAY,
            numberOfAppointments = 1,
            PersonGenerator.EVENT_1.id,
            uuid,
            requirementId = 2
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(eventSentenceRepository.existsById(appointment.eventId)).thenReturn(true)
        whenever(requirementRepository.existsById(appointment.requirementId!!)).thenReturn(false)
        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("Requirement with requirementId of 2 not found"))

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoMoreInteractions(eventSentenceRepository)
        verifyNoMoreInteractions(requirementRepository)
        verifyNoInteractions(licenceConditionRepository)
        verifyNoInteractions(appointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `licence not found`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(2),
            interval = CreateAppointment.Interval.DAY,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = uuid,
            licenceConditionId = 3
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(eventSentenceRepository.existsById(appointment.eventId)).thenReturn(true)
        whenever(licenceConditionRepository.existsById(appointment.licenceConditionId!!)).thenReturn(false)
        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("LicenceCondition with licenceConditionId of 3 not found"))

        verifyNoMoreInteractions(offenderManagerRepository)
        verifyNoMoreInteractions(eventSentenceRepository)
        verifyNoMoreInteractions(licenceConditionRepository)
        verifyNoInteractions(requirementRepository)
        verifyNoInteractions(appointmentRepository)
        verifyNoInteractions(appointmentTypeRepository)
    }

    @Test
    fun `error overlapping appointment`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.HomeVisitToCaseNS,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(2),
            numberOfAppointments = 3,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = uuid
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(staffUserRepository.findUserAndLocation(appointment.user.username, appointment.user.team))
            .thenReturn(UserLoc(1, 2, 3, 4, 5))

        whenever(eventSentenceRepository.existsById(appointment.eventId)).thenReturn(true)

        whenever(
            appointmentRepository.getClashCount(
                OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE.person.id,
                appointment.start.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                appointment.start.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault())),
                appointment.end.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault()))
            )
        ).thenReturn(1)

        assertThrows<ConflictException> { service.createAppointment(PersonGenerator.PERSON_1.crn, appointment) }
    }

    @Test
    fun `success create overlapping appointment`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.HomeVisitToCaseNS,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(2),
            numberOfAppointments = 3,
            eventId = PersonGenerator.EVENT_1.id,
            uuid = uuid,
            createOverlappingAppointment = true
        )

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(
            OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE
        )
        whenever(staffUserRepository.findUserAndLocation(appointment.user.username, appointment.user.team))
            .thenReturn(UserLoc(1, 2, 3, 4, 5))

        whenever(eventSentenceRepository.existsById(appointment.eventId)).thenReturn(true)

        whenever(appointmentTypeRepository.findByCode(appointment.type.code)).thenReturn(
            ContactType(
                1,
                appointment.type.code,
                true,
                "description"
            )
        )

        whenever(
            appointmentRepository.getClashCount(
                OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE.person.id,
                appointment.start.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                appointment.start.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault())),
                appointment.end.format(DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZoneId.systemDefault()))
            )
        ).thenReturn(1)

        service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
    }

    data class UserLoc(
        val _userId: Long,
        val _staffId: Long,
        val _teamId: Long,
        val _providerId: Long,
        val _locationId: Long,
    ) : UserLocation {
        override val userId: Long
            get() = _userId
        override val staffId: Long
            get() = _staffId
        override val teamId: Long
            get() = _teamId
        override val providerId: Long
            get() = _providerId
        override val locationId: Long
            get() = _locationId
    }
}