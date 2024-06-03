package uk.gov.justice.digital.hmpps.messaging

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.client.ManageOffencesClient
import uk.gov.justice.digital.hmpps.client.Offence
import uk.gov.justice.digital.hmpps.converter.NotificationConverter
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.COURT_CATEGORY
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.EXISTING_DETAILED_OFFENCE
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.EXISTING_OFFENCE
import uk.gov.justice.digital.hmpps.data.generator.DataGenerator.HIGH_LEVEL_OFFENCE
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

    @Test
    fun `missing reference data is thrown`() {
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offenceCode = notification.message.offenceCode
        whenever(manageOffencesClient.getOffence(offenceCode)).thenReturn(offence(offenceCode))

        assertThrows<NotFoundException> { handler.handle(notification) }
            .run { assertThat(message, equalTo("Court category with code of CS not found")) }
    }

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
        whenever(featureFlags.enabled(FF_CREATE_OFFENCE)).thenReturn(true)
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offence = offence(notification.message.offenceCode)
        whenever(manageOffencesClient.getOffence(offence.code)).thenReturn(offence)
        whenever(referenceDataRepository.findByCodeAndSetName(COURT_CATEGORY.code, COURT_CATEGORY.set.name))
            .thenReturn(COURT_CATEGORY)
        whenever(offenceRepository.findByCode(any())).thenReturn(null)
        whenever(offenceRepository.findByCode(HIGH_LEVEL_OFFENCE.code)).thenReturn(HIGH_LEVEL_OFFENCE)

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(detailedOffenceRepository).save(check {
            assertThat(it.id, equalTo(0))
            assertThat(it.code, equalTo(offence.code))
            assertThat(it.category, equalTo(COURT_CATEGORY))
        })
        verify(offenceRepository).save(check {
            assertThat(it.id, equalTo(0))
            assertThat(it.code, equalTo(offence.homeOfficeCode))
        })
        verify(telemetryService).trackEvent(
            "OffenceCodeCreated",
            mapOf("offenceCode" to offence.code, "homeOfficeCode" to "09155"),
            mapOf()
        )
    }

    @Test
    fun `offence is updated`() {
        whenever(featureFlags.enabled(FF_CREATE_OFFENCE)).thenReturn(true)
        whenever(featureFlags.enabled(FF_UPDATE_OFFENCE)).thenReturn(true)
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offence = offence(notification.message.offenceCode)
        whenever(manageOffencesClient.getOffence(offence.code)).thenReturn(offence)
        whenever(referenceDataRepository.findByCodeAndSetName(COURT_CATEGORY.code, COURT_CATEGORY.set.name))
            .thenReturn(COURT_CATEGORY)
        whenever(offenceRepository.findByCode(HIGH_LEVEL_OFFENCE.code)).thenReturn(HIGH_LEVEL_OFFENCE)
        whenever(offenceRepository.findByCode(EXISTING_OFFENCE.code)).thenReturn(EXISTING_OFFENCE)
        whenever(detailedOffenceRepository.findByCode(EXISTING_DETAILED_OFFENCE.code)).thenReturn(
            EXISTING_DETAILED_OFFENCE
        )

        handler.handle(notification)

        verify(telemetryService).notificationReceived(notification)
        verify(detailedOffenceRepository).save(check {
            assertThat(it.id, equalTo(EXISTING_DETAILED_OFFENCE.id))
            assertThat(it.code, equalTo(offence.code))
            assertThat(it.category, equalTo(COURT_CATEGORY))
        })
        verify(offenceRepository).save(check {
            assertThat(it.id, equalTo(EXISTING_OFFENCE.id))
            assertThat(it.code, equalTo(offence.homeOfficeCode))
        })
        verify(telemetryService).trackEvent(
            "OffenceCodeUpdated",
            mapOf("offenceCode" to offence.code, "homeOfficeCode" to "09155"),
            mapOf()
        )
    }

    @Test
    fun `offence is not updated when feature flag is disabled`() {
        whenever(featureFlags.enabled(FF_CREATE_OFFENCE)).thenReturn(true)
        whenever(featureFlags.enabled(FF_UPDATE_OFFENCE)).thenReturn(false)
        val notification = Notification(ResourceLoader.event("offence-changed"))
        val offence = offence(notification.message.offenceCode)
        whenever(manageOffencesClient.getOffence(offence.code)).thenReturn(offence)
        whenever(referenceDataRepository.findByCodeAndSetName(COURT_CATEGORY.code, COURT_CATEGORY.set.name))
            .thenReturn(COURT_CATEGORY)
        whenever(offenceRepository.findByCode(EXISTING_OFFENCE.code)).thenReturn(EXISTING_OFFENCE)
        whenever(detailedOffenceRepository.findByCode(EXISTING_DETAILED_OFFENCE.code)).thenReturn(
            EXISTING_DETAILED_OFFENCE
        )

        handler.handle(notification)

        verify(offenceRepository, never()).save(any())
        verify(detailedOffenceRepository).save(check {
            assertThat(it.id, equalTo(EXISTING_DETAILED_OFFENCE.id))
            assertThat(it.code, equalTo(offence.code))
            assertThat(it.category, equalTo(COURT_CATEGORY))
        })
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
