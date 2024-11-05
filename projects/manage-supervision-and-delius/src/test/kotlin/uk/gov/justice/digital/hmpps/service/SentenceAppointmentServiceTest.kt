package uk.gov.justice.digital.hmpps.service

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
import uk.gov.justice.digital.hmpps.exception.InvalidRequestException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.ZonedDateTime
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
    fun `until before end date`() {
        val appointment = CreateAppointment(
            user,
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
            start = ZonedDateTime.now().plusDays(2),
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
            null,
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
}