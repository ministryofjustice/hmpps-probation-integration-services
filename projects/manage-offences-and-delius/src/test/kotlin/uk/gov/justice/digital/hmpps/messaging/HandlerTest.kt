package uk.gov.justice.digital.hmpps.messaging

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.client.ManageOffencesClient
import uk.gov.justice.digital.hmpps.client.Offence
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.COURT_CATEGORY
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.service.OffenceService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryMessagingExtensions.notificationReceived
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
internal class HandlerTest {
    @Mock
    lateinit var converter: NotificationConverter<HmppsDomainEvent>

    @Mock
    lateinit var telemetryService: TelemetryService

    @Mock
    lateinit var manageOffencesClient: ManageOffencesClient

    @Mock
    lateinit var offenceService: OffenceService

    @InjectMocks
    lateinit var handler: Handler

    @Test
    fun `home office codes of 22222 are ignored`() {
        whenever(manageOffencesClient.getOffence(any())).thenReturn(offence(homeOfficeCode = "222/22"))

        handler.handle(Notification(ResourceLoader.event("offence-changed")))

        verify(telemetryService).trackEvent(
            "OffenceCodeIgnored",
            mapOf(
                "offenceCode" to "AB12345",
                "homeOfficeCode" to "22222",
                "reason" to "Home Office Code is 'Not Known'"
            ),
            mapOf()
        )
    }

    @Test
    fun `cjs codes ending with a number of 500 or above are ignored`() {
        whenever(manageOffencesClient.getOffence(any())).thenReturn(offence(code = "AB12500"))

        handler.handle(Notification(ResourceLoader.event("offence-changed")))

        verify(telemetryService).trackEvent(
            "OffenceCodeIgnored",
            mapOf(
                "offenceCode" to "AB12500",
                "homeOfficeCode" to "09155",
                "reason" to "CJS Code suffix is 500 or above"
            ),
            mapOf()
        )
    }

    @Test
    fun `offence is created`() {
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offence = offence(notification.message.offenceCode)
        whenever(manageOffencesClient.getOffence(notification.message.offenceCode)).thenReturn(offence)
        whenever(offenceService.createOffence(offence)).thenReturn(true)

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(offenceService).createOffence(offence)
        verify(telemetryService).trackEvent(
            "OffenceCodeCreated",
            mapOf("offenceCode" to offence.code, "homeOfficeCode" to "09155"),
            mapOf()
        )
    }

    private fun offence(code: String = "AB12345", homeOfficeCode: String = "091/55") = Offence(
        code = code,
        description = "some offence",
        offenceType = COURT_CATEGORY.code,
        startDate = LocalDate.now(),
        homeOfficeStatsCode = homeOfficeCode,
        homeOfficeDescription = "Some Offence Description",
    )
}
