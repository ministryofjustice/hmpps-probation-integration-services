package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.core.ParameterizedTypeReference
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.detail.DomainEventDetailService
import uk.gov.justice.digital.hmpps.integration.EntityType
import uk.gov.justice.digital.hmpps.integration.ReferralStart
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.message.*
import uk.gov.justice.digital.hmpps.service.ComponentCommencementService
import uk.gov.justice.digital.hmpps.service.ComponentTerminationService
import uk.gov.justice.digital.hmpps.service.StatusChangeService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.net.URI
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

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

    @Mock
    lateinit var componentCommencementService: ComponentCommencementService

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `status changed telemetry is tracked when contact is created`() {
        val notification = statusChangedNotification()
        val detail = statusInfo()
        givenStatusDetail(notification, detail)
        whenever(
            statusChangeService.statusChanged(
                notification.id,
                "A000001",
                notification.message.occurredAt,
                detail
            )
        ).thenReturn(true)

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
        whenever(
            statusChangeService.statusChanged(
                notification.id,
                "A000001",
                notification.message.occurredAt,
                detail
            )
        ).thenReturn(false)

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(statusChangeService).statusChanged(notification.id, "A000001", notification.message.occurredAt, detail)
        verify(telemetryService, never()).trackEvent(
            eq("StatusChanged"),
            any<Map<String, String?>>(),
            any<Map<String, Double?>>()
        )
    }

    @Test
    fun `programme started notification commences component`() {
        val notification = startedNotification()
        val uri = URI.create(notification.message.detailUrl)
        val detail = ReferralStart(
            "123",
            LocalDateTime.parse("2025-04-01T09:30:00"),
            EntityType.REQUIREMENT
        )
        val result = ComponentCommencementService.Result(
            ComponentCommencementService.Result.ResultName.ComponentCommenced,
            mapOf("componentId" to "123")
        )
        whenever(detailService.validate(notification.message.detailUrl)).thenReturn(uri)
        whenever(detailService.getDetail(eq(uri), any<ParameterizedTypeReference<ReferralStart>>())).thenReturn(detail)
        whenever(componentCommencementService.commence("A000001", detail)).thenReturn(result)

        handler.handle(notification)

        verify(componentCommencementService).commence("A000001", detail)
        verify(telemetryService).trackEvent("ComponentCommenced", result.data)
    }

    private fun startedNotification() = Notification(
        message = HmppsDomainEvent(
            eventType = Handler.REFERRAL_STARTED,
            detailUrl = "http://localhost/referral/00000000-0000-0000-0000-000000000001/start-data",
            occurredAt = ZonedDateTime.parse("2025-09-09T09:15:30+01:00"),
            description = "Programme started",
            personReference = PersonReference(listOf(PersonIdentifier("CRN", "A000001")))
        ),
        attributes = MessageAttributes(Handler.REFERRAL_STARTED),
        id = UUID.fromString("bb3c2828-167f-529b-8e19-73735a2fb85c")
    )

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
