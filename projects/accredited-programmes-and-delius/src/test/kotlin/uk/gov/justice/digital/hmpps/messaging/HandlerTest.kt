package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.core.ParameterizedTypeReference
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integration.EntityType
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.service.ComponentTerminationService
import uk.gov.justice.digital.hmpps.service.StatusChangeService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    lateinit var detailService: DomainEventDetailService

    @Mock
    lateinit var statusChangeService: StatusChangeService

    @Mock
    lateinit var componentTerminationService: ComponentTerminationService

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `status changed telemetry is tracked when contact is created`() {
        val notification = statusChangedNotification()
        val detail = statusInfo()
        givenStatusDetail(notification, detail)
        whenever(statusChangeService.statusChanged(notification.id, "A000001", notification.message.occurredAt, detail)).thenReturn(true)

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(statusChangeService).statusChanged(notification.id, "A000001", notification.message.occurredAt, detail)
        verify(telemetryService).trackEvent(
            "StatusChanged",
            mapOf(
                "crn" to "A000001",
                "componentId" to detail.sourcedFromEntityId.toString(),
                "status" to detail.newStatus.toString(),
                "username" to detail.username,
            )
        )
    }

    @Test
    fun `status changed telemetry is not tracked when component is ignored`() {
        val notification = statusChangedNotification()
        val detail = statusInfo()
        givenStatusDetail(notification, detail)
        whenever(statusChangeService.statusChanged(notification.id, "A000001", notification.message.occurredAt, detail)).thenReturn(false)

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(statusChangeService).statusChanged(notification.id, "A000001", notification.message.occurredAt, detail)
        verify(telemetryService, never()).trackEvent(
            eq("StatusChanged"),
            any<Map<String, String?>>(),
            any<Map<String, Double?>>()
        )
    }

    private fun givenStatusDetail(notification: Notification<HmppsDomainEvent>, detail: StatusInfo) {
        val detailUrl = requireNotNull(notification.message.detailUrl)
        val uri = URI.create(detailUrl)
        whenever(detailService.validate(detailUrl)).thenReturn(uri)
        whenever(detailService.getDetail(eq(uri), any<ParameterizedTypeReference<StatusInfo>>())).thenReturn(detail)
    }

    private fun statusChangedNotification() = Notification(
        message = HmppsDomainEvent(
            eventType = Handler.REFERRAL_STATUS_CHANGED,
            detailUrl = "http://localhost/status/123",
            occurredAt = ZonedDateTime.parse("2025-09-09T09:15:30+01:00"),
            description = "Status changed",
            personReference = PersonReference(listOf(PersonIdentifier("CRN", "A000001")))
        ),
        attributes = MessageAttributes(Handler.REFERRAL_STATUS_CHANGED),
        id = UUID.fromString("aa2c2828-167f-529b-8e19-73735a2fb85c")
    )


    private fun statusInfo() = StatusInfo(
        newStatus = StatusInfo.Status.BREACH,
        sourcedFromEntityType = EntityType.LICENCE_CONDITION,
        sourcedFromEntityId = 123L,
        notes = "Some notes",
        description = "Some description",
        username = "testuser"
    )
}

