package uk.gov.justice.digital.hmpps.scheduling

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.service.DomainEventService

@Service
class Scheduler(private val service: DomainEventService) {
    @Transactional
    @Scheduled(fixedDelayString = "\${poller.fixed-delay:100}")
    fun poll() {
        service.getDeltas()
            .onEach { service.notify(it) }
            .also(service::deleteAll)
    }
}
