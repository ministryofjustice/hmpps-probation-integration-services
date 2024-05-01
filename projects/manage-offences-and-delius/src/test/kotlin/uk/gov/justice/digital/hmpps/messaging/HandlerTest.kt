package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.check
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.client.ManageOffencesClient
import uk.gov.justice.digital.hmpps.client.Offence
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.COURT_CATEGORY
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.EXISTING_OFFENCE
import uk.gov.justice.digital.hmpps.entity.OffenceRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.repository.DetailedOffenceRepository
import uk.gov.justice.digital.hmpps.repository.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.telemetry.notificationReceived
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
    lateinit var detailedOffenceRepository: DetailedOffenceRepository

    @Mock
    lateinit var offenceRepository: OffenceRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var featureFlags: FeatureFlags

    @InjectMocks
    lateinit var handler: Handler

    @BeforeEach
    fun setup() {
        whenever(featureFlags.enabled(FF_CREATE_OFFENCE)).thenReturn(false)
    }

    @Test
    fun `missing reference data is thrown`() {
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offenceCode = notification.message.offenceCode
        whenever(manageOffencesClient.getOffence(offenceCode)).thenReturn(offence(offenceCode))

        assertThrows<NotFoundException> { handler.handle(notification) }
            .run { assertThat(message, equalTo("Court category with code of CS not found")) }
    }

    @Test
    fun `offence is created`() {
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offenceCode = notification.message.offenceCode
        whenever(manageOffencesClient.getOffence(offenceCode)).thenReturn(offence(offenceCode))
        whenever(referenceDataRepository.findByCodeAndSetName(COURT_CATEGORY.code, COURT_CATEGORY.set.name)).thenReturn(
            COURT_CATEGORY
        )

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(detailedOffenceRepository).save(
            check {
                assertThat(it.id, equalTo(0))
                assertThat(it.code, equalTo(offenceCode))
                assertThat(it.category, equalTo(COURT_CATEGORY))
            }
        )
        verify(telemetryService).trackEvent(
            "OffenceCodeCreated",
            mapOf("offenceCode" to offenceCode, "homeOfficeCode" to "01234"),
            mapOf()
        )
    }

    @Test
    fun `offence is updated`() {
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offenceCode = notification.message.offenceCode
        whenever(manageOffencesClient.getOffence(offenceCode)).thenReturn(offence(notification.message.offenceCode))
        whenever(referenceDataRepository.findByCodeAndSetName(COURT_CATEGORY.code, COURT_CATEGORY.set.name)).thenReturn(
            COURT_CATEGORY
        )
        whenever(detailedOffenceRepository.findByCode(EXISTING_OFFENCE.code)).thenReturn(EXISTING_OFFENCE)

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(detailedOffenceRepository).save(
            check {
                assertThat(it.id, equalTo(EXISTING_OFFENCE.id))
                assertThat(it.code, equalTo(offenceCode))
                assertThat(it.category, equalTo(COURT_CATEGORY))
            }
        )
        verify(telemetryService).trackEvent(
            "OffenceCodeUpdated",
            mapOf("offenceCode" to offenceCode, "homeOfficeCode" to "01234"),
            mapOf()
        )
    }

    private fun offence(code: String) = Offence(
        code = code,
        description = "some offence",
        offenceType = COURT_CATEGORY.code,
        startDate = LocalDate.now(),
        homeOfficeStatsCode = "012/34",
        homeOfficeDescription = "Some Offence Description",
    )
}
