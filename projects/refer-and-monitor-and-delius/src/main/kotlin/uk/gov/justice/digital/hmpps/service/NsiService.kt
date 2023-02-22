package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.appointment.AppointmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.NsiRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referral.getByCrnAndExternalReference
import uk.gov.justice.digital.hmpps.messaging.NsiTermination

@Service
class NsiService(
    private val nsiRepository: NsiRepository,
    private val appointmentRepository: AppointmentRepository
) {
    fun terminateNsi(termination: NsiTermination) {
        val nsi = nsiRepository.getByCrnAndExternalReference(termination.crn, termination.urn)
        appointmentRepository.deleteFutureAppointmentsForNsi(nsi.id)
    }
}