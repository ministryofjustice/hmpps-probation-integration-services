package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.DEFAULT_UPW_PROJECT
import uk.gov.justice.digital.hmpps.data.generator.UPWGenerator.SECOND_UPW_PROJECT
import uk.gov.justice.digital.hmpps.integrations.delius.entity.*
import uk.gov.justice.digital.hmpps.model.AppointmentOutcomeRequest
import uk.gov.justice.digital.hmpps.model.Behaviour
import uk.gov.justice.digital.hmpps.model.Code
import uk.gov.justice.digital.hmpps.model.WorkQuality
import java.time.LocalTime
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class AppointmentsServiceTest {
    @Mock
    lateinit var unpaidWorkProjectRepository: UnpaidWorkProjectRepository

    @Mock
    lateinit var unpaidWorkAppointmentRepository: UnpaidWorkAppointmentRepository

    @Mock
    lateinit var contactOutcomeRepository: ContactOutcomeRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var contactAlertRepository: ContactAlertRepository

    @Mock
    lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    lateinit var enforcementRepository: EnforcementRepository

    @Mock
    lateinit var enforcementActionRepository: EnforcementActionRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var userAccessService: UserAccessService

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @InjectMocks
    lateinit var appointmentsService: AppointmentsService

    @Test
    fun `updating appointments in the past require an outcome`() {
        whenever(unpaidWorkAppointmentRepository.getUpwAppointmentById(UPWGenerator.UPW_APPOINTMENT_PAST.id))
            .thenReturn(UPWGenerator.UPW_APPOINTMENT_PAST)

        val exception = assertThrows<IllegalArgumentException> {
            appointmentsService.updateAppointmentOutcome(
                SECOND_UPW_PROJECT.code,
                UPWGenerator.UPW_APPOINTMENT_PAST.id,
                AppointmentOutcomeRequest(
                    id = UPWGenerator.UPW_APPOINTMENT_PAST.id,
                    version = UUID(1, 1),
                    outcome = null,
                    supervisor = Code("N01P001"),
                    startTime = LocalTime.of(9, 0),
                    endTime = LocalTime.of(17, 0),
                    notes = null,
                    hiVisWorn = false,
                    workedIntensively = false,
                    penaltyMinutes = 0L,
                    minutesCredited = null,
                    workQuality = WorkQuality.GOOD,
                    behaviour = Behaviour.GOOD,
                    sensitive = false,
                    alertActive = false
                )
            )
        }

        assertThat(exception.message).isEqualTo("Appointments in the past require an outcome")
    }

    @Test
    fun `endTime must be after startTime when updating appointment`() {
        whenever(unpaidWorkAppointmentRepository.getUpwAppointmentById(UPWGenerator.DEFAULT_UPW_APPOINTMENT.id))
            .thenReturn(UPWGenerator.DEFAULT_UPW_APPOINTMENT)

        val exception = assertThrows<IllegalArgumentException> {
            appointmentsService.updateAppointmentOutcome(
                DEFAULT_UPW_PROJECT.code,
                UPWGenerator.DEFAULT_UPW_APPOINTMENT.id,
                AppointmentOutcomeRequest(
                    id = UPWGenerator.DEFAULT_UPW_APPOINTMENT.id,
                    version = UUID(1, 1),
                    outcome = null,
                    supervisor = Code("N01P001"),
                    startTime = LocalTime.of(12, 0),
                    endTime = LocalTime.of(10, 0),
                    notes = null,
                    hiVisWorn = false,
                    workedIntensively = false,
                    penaltyMinutes = 0L,
                    minutesCredited = null,
                    workQuality = WorkQuality.GOOD,
                    behaviour = Behaviour.GOOD,
                    sensitive = false,
                    alertActive = false
                )
            )
        }

        assertThat(exception.message).isEqualTo("End Time must be after Start Time")
    }
}