package uk.gov.justice.digital.hmpps.services.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.Notifier
import uk.gov.justice.digital.hmpps.publisher.NotificationPublisher
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
@ExtendWith(OutputCaptureExtension::class)
class NotifierTest {

    val noms = "CRN123"

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var queuePublisher: NotificationPublisher

    lateinit var notifier: Notifier

    @BeforeEach
    fun setUp() {
        notifier = Notifier("localhost", personRepository, queuePublisher)
    }

    @Test
    fun `test notification`(output: CapturedOutput) {
        whenever(personRepository.findNomsSingleCustodial()).thenReturn(Stream.of(noms))
        doNothing().whenever(queuePublisher).publish(any<Notification<*>>())
        notifier.requestBulkUpdate(true)

        verify(personRepository, times(1)).findNomsSingleCustodial()
        verify(queuePublisher, times(1)).publish(any<Notification<*>>())

        verifyNoMoreInteractions(personRepository)
        verifyNoMoreInteractions(queuePublisher)

        assertThat(output.toString(), containsString("Published 1 messages successfully"))
    }
}