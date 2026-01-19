package uk.gov.justice.digital.hmpps.appointments.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.AppointmentContact
import uk.gov.justice.digital.hmpps.appointments.entity.AppointmentEntities.PersonManager
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.AlertRepository
import uk.gov.justice.digital.hmpps.appointments.repository.AppointmentRepositories.PersonManagerRepository
import uk.gov.justice.digital.hmpps.appointments.test.TestData
import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id

@ExtendWith(MockitoExtension::class)
class AlertServiceTest {
    @Mock
    private lateinit var alertRepository: AlertRepository

    @Mock
    private lateinit var personManagerRepository: PersonManagerRepository

    @InjectMocks
    private lateinit var alertService: AlertService

    @Test
    fun `creates alert successfully`() {
        val appointment = TestData.appointment()
        val manager = PersonManager(id(), appointment.personId, id(), id())
        whenever(personManagerRepository.getActiveManagerForPerson(appointment.personId)).thenReturn(manager)

        alertService.createAlert(appointment)

        verify(alertRepository).save(check {
            assertThat(it.personId).isEqualTo(appointment.personId)
            assertThat(it.appointmentId).isEqualTo(appointment.id)
            assertThat(it.appointmentTypeId).isEqualTo(appointment.type.id)
            assertThat(it.managerId).isEqualTo(manager.id)
            assertThat(it.staffId).isEqualTo(manager.staffId)
            assertThat(it.teamId).isEqualTo(manager.teamId)
        })
    }

    @Test
    fun `removes alert successfully`() {
        val appointment = TestData.appointment()

        alertService.removeAlert(appointment)

        verify(alertRepository).deleteByAppointmentId(appointment.id!!)
    }

    @Test
    fun `does nothing if appointment has not yet been saved`() {
        val appointment = mock<AppointmentContact>()
        whenever(appointment.id).thenReturn(null)

        alertService.removeAlert(appointment)

        verify(alertRepository, never()).deleteByAppointmentId(any())
    }
}
