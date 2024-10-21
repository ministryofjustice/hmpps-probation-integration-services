package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher

@ExtendWith(MockitoExtension::class)
@ExtendWith(OutputCaptureExtension::class)
class NotifierTest {
    @Mock
    lateinit var topicPublisher: NotificationPublisher

    lateinit var notifier: Notifier

    @BeforeEach
    fun setUp() {
        notifier = Notifier(topicPublisher)
    }

    @Test
    fun `test notification`(output: CapturedOutput) {
        doNothing().whenever(topicPublisher).publish(any<Notification<*>>())
        notifier.caseCreated(PersonGenerator.DEFAULT)
        verify(topicPublisher, times(1)).publish(any<Notification<*>>())
        verifyNoMoreInteractions(topicPublisher)
    }
}