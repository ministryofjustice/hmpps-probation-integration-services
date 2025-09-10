package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integration.AccreditedProgrammesClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.net.URI

@Service
class ReferralStatusChanged(
    private val accreditedProgrammes: AccreditedProgrammesClient,
    private val appointmentService: AppointmentService,
) {
    fun handle(domainEvent: HmppsDomainEvent) {
        domainEvent.detailUrl?.let {
            accreditedProgrammes.getStatusInfo(URI.create(it))
        }?.also {
            appointmentService.statusChanged(
                requireNotNull(domainEvent.personReference.findCrn()),
                domainEvent.occurredAt,
                it
            )
        }
    }
}