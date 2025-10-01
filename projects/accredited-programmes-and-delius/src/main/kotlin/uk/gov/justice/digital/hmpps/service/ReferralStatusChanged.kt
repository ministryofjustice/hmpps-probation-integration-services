package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.util.UUID

@Service
class ReferralStatusChanged(
    private val detailService: DomainEventDetailService,
    private val appointmentService: AppointmentService,
) {
    fun handle(messageId: UUID, domainEvent: HmppsDomainEvent) {
        domainEvent.detailUrl?.let {
            detailService.getDetail<StatusInfo>(domainEvent)
        }?.also {
            appointmentService.statusChanged(
                messageId,
                requireNotNull(domainEvent.personReference.findCrn()),
                domainEvent.occurredAt,
                it
            )
        }
    }
}