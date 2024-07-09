package uk.gov.justice.digital.hmpps.integrations.delius

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.integrations.delius.offender.OffenderDeltaService

@Service
class OffenderDeltaPoller(private val service: OffenderDeltaService) {
    @Transactional
    @Scheduled(fixedDelayString = "\${offender-events.fixed-delay:100}")
    fun poll() {
        service.getDeltas()
            .onEach { service.notify(it) }
            .also(service::deleteAll)
    }
}
