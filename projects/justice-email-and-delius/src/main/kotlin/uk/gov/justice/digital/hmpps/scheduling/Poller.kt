package uk.gov.justice.digital.hmpps.scheduling

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.service.MailboxService

@Service
class Poller(private val mailboxService: MailboxService) {
    @Scheduled(fixedDelayString = "\${poller.fixed-delay:60000}")
    fun poll() {
        mailboxService.publishUnreadMessagesToQueue()
    }
}
