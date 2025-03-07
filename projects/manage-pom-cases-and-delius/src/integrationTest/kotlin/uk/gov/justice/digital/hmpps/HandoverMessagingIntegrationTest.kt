package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.MessageGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.KeyDateRepository
import uk.gov.justice.digital.hmpps.integrations.delius.allocation.entity.event.keydate.findHandoverDates
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.services.KeyDateMergeResult
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@SpringBootTest
internal class HandoverMessagingIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Value("\${mpc.handover.url}")
    lateinit var handoverUrl: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var custodyRepository: CustodyRepository

    @Autowired
    lateinit var keyDateRepository: KeyDateRepository

    @Test
    fun `updates a handover key date and start date successfully`() {
        val notification = prepNotification(
            notification("update-handover-and-start-date"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            KeyDateMergeResult.KeyDateUpdated.name,
            mapOf(
                "nomsId" to "A2048BY",
                "handoverDate" to "2023-05-09",
                "handoverStartDate" to "2023-05-04",
                "existingPOM1" to "2023-05-04",
                "existingPOM2" to "2023-05-09",
            )
        )

        val custody =
            custodyRepository.findAllByDisposalEventPersonId(PersonGenerator.UPDATE_HANDOVER_AND_START.id).first()
        val handoverDates = keyDateRepository.findHandoverDates(custody.id).associateBy { it.type.code }
        assertThat(handoverDates.size, equalTo(2))
        assertThat(handoverDates[KeyDate.TypeCode.HANDOVER_DATE.value]?.date, equalTo(LocalDate.of(2023, 5, 9)))
        assertThat(handoverDates[KeyDate.TypeCode.HANDOVER_START_DATE.value]?.date, equalTo(LocalDate.of(2023, 5, 4)))
    }

    @Test
    fun `creates a handover key date and start date successfully`() {
        val notification = prepNotification(
            notification("create-handover-and-start-date"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            KeyDateMergeResult.KeyDateCreated.name,
            mapOf(
                "nomsId" to "A4096BY",
                "handoverDate" to "2023-05-10",
                "handoverStartDate" to "2023-05-06"
            )
        )

        val custody =
            custodyRepository.findAllByDisposalEventPersonId(PersonGenerator.CREATE_HANDOVER_AND_START.id).first()
        val handoverDates = keyDateRepository.findHandoverDates(custody.id).associateBy { it.type.code }
        assertThat(handoverDates.size, equalTo(2))
        assertThat(handoverDates[KeyDate.TypeCode.HANDOVER_DATE.value]?.date, equalTo(LocalDate.of(2023, 5, 10)))
        assertThat(handoverDates[KeyDate.TypeCode.HANDOVER_START_DATE.value]?.date, equalTo(LocalDate.of(2023, 5, 6)))
    }

    @Test
    fun `processes a sentence changed event successfully`() {
        val notification = Notification(
            message = MessageGenerator.SENTENCE_CHANGED,
            attributes = MessageAttributes(eventType = "SENTENCE_CHANGED")
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            KeyDateMergeResult.KeyDateCreated.name,
            mapOf(
                "nomsId" to "A4096CY",
                "handoverDate" to "2023-06-11",
                "handoverStartDate" to "2023-06-07"
            )
        )

        val custody =
            custodyRepository.findAllByDisposalEventPersonId(PersonGenerator.CREATE_SENTENCE_CHANGED.id).first()
        val handoverDates = keyDateRepository.findHandoverDates(custody.id).associateBy { it.type.code }
        assertThat(handoverDates.size, equalTo(2))
        assertThat(handoverDates[KeyDate.TypeCode.HANDOVER_DATE.value]?.date, equalTo(LocalDate.of(2023, 6, 11)))
        assertThat(handoverDates[KeyDate.TypeCode.HANDOVER_START_DATE.value]?.date, equalTo(LocalDate.of(2023, 6, 7)))
    }

    @Test
    fun `ignores a sentence changed event causing 404`() {
        val notification = Notification(
            message = MessageGenerator.SENTENCE_CHANGED_NOT_FOUND,
            attributes = MessageAttributes(eventType = "SENTENCE_CHANGED")
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(telemetryService).trackEvent(
            "Handovers api returned not found for sentence changed event",
            mapOf(
                "detailUrl" to "$handoverUrl/api/handovers/A4096DY"
            )
        )
    }

    @Test
    fun `ignores a sentence changed event due to multiple custodial sentences`() {
        val notification = Notification(
            message = MessageGenerator.SENTENCE_CHANGED_MULTIPLE_CUSTODIAL,
            attributes = MessageAttributes(eventType = "SENTENCE_CHANGED")
        )

        channelManager.getChannel(queueName).publishAndWait(notification)
        //Check only 1 telemetry event
        verify(telemetryService, times(1)).trackEvent(any(), any(), any())
    }
}
