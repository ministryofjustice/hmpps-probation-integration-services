package uk.gov.justice.digital.hmpps.appointments.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.Type.Companion.REVIEW_ENFORCEMENT_STATUS
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.AppointmentRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.EnforcementRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.EventRepository
import uk.gov.justice.digital.hmpps.appointments.test.TestData
import uk.gov.justice.digital.hmpps.set

@ExtendWith(MockitoExtension::class)
class EnforcementServiceTest() {
    @Mock
    private lateinit var enforcementRepository: EnforcementRepository

    @Mock
    private lateinit var appointmentRepository: AppointmentRepository

    @Mock
    private lateinit var eventRepository: EventRepository

    @InjectMocks
    private lateinit var enforcementService: EnforcementService

    @Test
    fun `creates enforcement and contact if it doesn't exist`() {
        val appointment = TestData.appointment(notes = "Some notes")
        whenever(enforcementRepository.existsByContactId(appointment.id!!)).thenReturn(false)

        enforcementService.applyEnforcementAction(appointment, TestData.ACTION, TestData.REVIEW_TYPE)

        verify(enforcementRepository).save(any())
        verify(appointmentRepository).save(check<AppointmentContact> {
            assertThat(it.type).isEqualTo(TestData.ACTION.type)
            assertThat(it.linkedContact).isEqualTo(appointment)
            assertThat(it.notes).matches(
                """
                Some notes
                
                \d{2}/\d{2}/\d{4} \d{2}:\d{2}
                Enforcement Action: Action 1
                """.trimIndent()
            )
        })
        assertThat(appointment.enforcement).isEqualTo(true)
        assertThat(appointment.enforcementActionId).isEqualTo(TestData.ACTION.id)
    }

    @Test
    fun `doesn't create an enforcement if it already exists`() {
        val appointment = TestData.appointment()
        whenever(enforcementRepository.existsByContactId(appointment.id!!)).thenReturn(true)

        enforcementService.applyEnforcementAction(appointment, TestData.ACTION, TestData.REVIEW_TYPE)

        verify(enforcementRepository, never()).save(any())
        verify(appointmentRepository, never()).save(any())
    }

    @Test
    fun `increments ftc count and creates review contact if limit exceeded`() {
        val event = TestData.event()
        val disposal = TestData.disposal(event = event, ftcLimit = 2)
        val appointment = TestData.appointment(event = event)
        event.set("disposal", disposal)

        whenever(enforcementRepository.existsByContactId(appointment.id!!)).thenReturn(true)
        whenever(appointmentRepository.countFailureToComply(event)).thenReturn(3)
        whenever(appointmentRepository.enforcementReviewExists(event.id, event.breachEnd)).thenReturn(false)

        enforcementService.applyEnforcementAction(appointment, TestData.ACTION, TestData.REVIEW_TYPE)

        verify(appointmentRepository).save(check<AppointmentContact> {
            assertThat(it.type).isEqualTo(TestData.REVIEW_TYPE)
        })
        assertThat(event.ftcCount).isEqualTo(3L)
    }

    @Test
    fun `review contact not created if it already exists`() {
        val event = TestData.event()
        val disposal = TestData.disposal(event = event, ftcLimit = 2)
        val appointment = TestData.appointment(event = event)
        event.set("disposal", disposal)

        whenever(enforcementRepository.existsByContactId(appointment.id!!)).thenReturn(true)
        whenever(appointmentRepository.countFailureToComply(event)).thenReturn(3)
        whenever(appointmentRepository.enforcementReviewExists(event.id, event.breachEnd)).thenReturn(true)

        enforcementService.applyEnforcementAction(appointment, TestData.ACTION, TestData.REVIEW_TYPE)

        verify(appointmentRepository, never()).save(check {
            assertThat(it.type.code).isEqualTo(REVIEW_ENFORCEMENT_STATUS)
        })
    }

    @Test
    fun `does not create review contact if limit not exceeded`() {
        val event = TestData.event()
        val disposal = TestData.disposal(event = event, ftcLimit = 2)
        val eventWithDisposal = TestData.event(disposal = disposal)
        val appointment = TestData.appointment(event = eventWithDisposal)

        whenever(enforcementRepository.existsByContactId(appointment.id!!)).thenReturn(true)
        whenever(appointmentRepository.countFailureToComply(eventWithDisposal)).thenReturn(2)

        enforcementService.applyEnforcementAction(appointment, TestData.ACTION, TestData.REVIEW_TYPE)

        verify(appointmentRepository, never()).save(check {
            assertThat(it.type.code).isEqualTo(REVIEW_ENFORCEMENT_STATUS)
        })
    }
}
