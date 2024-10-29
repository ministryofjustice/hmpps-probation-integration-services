package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.appointment.CreateAppointment
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.OffenderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.*
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
class AppointmentServiceTest {

    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    lateinit var appointmentRepository: AppointmentRepository

    @Mock
    lateinit var  appointmentTypeRepository: AppointmentTypeRepository

    @Mock
    lateinit var  offenderManagerRepository: OffenderManagerRepository

    @Mock
    lateinit var  eventSentenceRepository: EventSentenceRepository

    @Mock
    lateinit var requirementRepository: RequirementRepository

    @Mock
    lateinit var  licenceConditionRepository: LicenceConditionRepository

    @InjectMocks
    lateinit var service: AppointmentService


    @Test
    fun `event not found`() {
        val appointment = CreateAppointment(
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(2),
                        1,
            1)

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE)
        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("Event with eventId of 1 not found"))
    }

    @Test
    fun `requirement not found`() {
        val appointment = CreateAppointment(
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(2),
            1,
            PersonGenerator.EVENT_1.id,
            requirementId = 2)

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE)
        whenever(eventSentenceRepository.existsById(appointment.eventId)).thenReturn(true)
        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("Requirement with requirementId of 2 not found"))
    }

    @Test
    fun `licence not found`() {
        val appointment = CreateAppointment(
            CreateAppointment.Type.InitialAppointmentInOfficeNS,
            ZonedDateTime.now().plusDays(1),
            ZonedDateTime.now().plusDays(2),
            1,
            PersonGenerator.EVENT_1.id,
            licenceConditionId = 3)

        whenever(offenderManagerRepository.findByPersonCrnAndSoftDeletedIsFalseAndActiveIsTrue(PersonGenerator.PERSON_1.crn)).thenReturn(OffenderManagerGenerator.OFFENDER_MANAGER_ACTIVE)
        whenever(eventSentenceRepository.existsById(appointment.eventId)).thenReturn(true)
        whenever(licenceConditionRepository.existsById(appointment.licenceConditionId!!)).thenReturn(false)
        val exception = assertThrows<NotFoundException> {
            service.createAppointment(PersonGenerator.PERSON_1.crn, appointment)
        }

        assertThat(exception.message, equalTo("LicenceCondition with licenceConditionId of 3 not found"))
    }
}