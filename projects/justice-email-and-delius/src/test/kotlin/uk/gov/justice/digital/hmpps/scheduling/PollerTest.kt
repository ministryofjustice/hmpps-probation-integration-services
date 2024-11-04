package uk.gov.justice.digital.hmpps.scheduling

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import uk.gov.justice.digital.hmpps.service.MailboxService

@ExtendWith(MockitoExtension::class)
internal class PollerTest {
    @Mock
    lateinit var mailboxService: MailboxService

    @InjectMocks
    lateinit var poller: Poller

    @Test
    fun `poller calls service`() {
        poller.poll()
        verify(mailboxService).publishUnreadMessagesToQueue()
    }
}