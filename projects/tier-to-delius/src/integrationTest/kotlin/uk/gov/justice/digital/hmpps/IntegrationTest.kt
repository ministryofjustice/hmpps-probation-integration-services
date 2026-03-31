package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.repository.ContactDevRepository
import uk.gov.justice.digital.hmpps.data.repository.ManagementTierDevRepository
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.NotificationExtensions.withCrn
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class IntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}")
    private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val referenceDataRepository: ReferenceDataRepository,
    private val personRepository: PersonRepository,
    private val managementTierDevRepository: ManagementTierDevRepository,
    private val contactDevRepository: ContactDevRepository,
    private val wireMockServer: WireMockServer,
    @MockitoBean private val featureFlags: FeatureFlags,
    @MockitoBean private val telemetryService: TelemetryService
) {

    @Test
    @Order(1)
    fun `updates a tier`() {
        whenever(featureFlags.enabled("tier-to-delius-v3")).thenReturn(true)
        val notification = prepEvent("tier-calculation", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val expectedTier = referenceDataRepository.findByCodeAndSetName("SPB", "TIER")!!
        val expectedReason = referenceDataRepository.findByCodeAndSetName("ATS", "TIER CHANGE REASON")!!

        val person = personRepository.findByCrnAndSoftDeletedIsFalse(notification.message.personReference.findCrn()!!)!!
        assertThat(person.currentTier, equalTo(expectedTier.id))

        val managementTier = managementTierDevRepository.findByIdPersonId(person.id)
        assertThat(managementTier.id.tierId, equalTo(expectedTier.id))
        assertThat(managementTier.tierChangeReasonId, equalTo(expectedReason.id))

        val contact = contactDevRepository.findByPersonId(person.id)
        assertThat(contact.type.code, equalTo(ContactTypeCode.TIER_UPDATE.code))

        verify(telemetryService).trackEvent(
            "TierUpdateSuccess",
            mapOf(
                "crn" to "A000001",
                "tier" to "B",
                "calculationId" to "123e4567-e89b-12d3-a456-426614174000",
                "calculationDate" to ZonedDateTimeDeserializer.deserialize("2021-04-23T18:25:43.511Z").toString()
            )
        )
    }

    @Test
    @Order(2)
    fun `end-dates previous tier`() {
        whenever(featureFlags.enabled("tier-to-delius-v3")).thenReturn(true)
        val notification = prepEvent("tier-update", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(notification)

        val expectedTier = referenceDataRepository.findByCodeAndSetName("SPA", "TIER")!!
        val person = personRepository.findByCrnAndSoftDeletedIsFalse(notification.message.personReference.findCrn()!!)!!
        assertThat(person.currentTier, equalTo(expectedTier.id))

        val managementTiers = managementTierDevRepository.findAllByIdPersonIdOrderByIdDateChanged(person.id)
        assertThat(
            managementTiers.first().endDate!!.toEpochSecond(),
            equalTo(ZonedDateTime.parse("2021-04-24T18:25:43.511Z").toEpochSecond())
        )
    }

    @Test
    @Order(1)
    fun `updates a tier when feature flag is disabled`() {
        whenever(featureFlags.enabled("tier-to-delius-v3")).thenReturn(false)
        val notification = prepEvent("tier-calculation", wireMockServer.port()).withCrn("A000002")

        channelManager.getChannel(queueName).publishAndWait(notification)

        val expectedTier = referenceDataRepository.findByCodeAndSetName("UD2", "TIER")!!
        val expectedReason = referenceDataRepository.findByCodeAndSetName("ATS", "TIER CHANGE REASON")!!

        val person = personRepository.findByCrnAndSoftDeletedIsFalse(notification.message.personReference.findCrn()!!)!!
        assertThat(person.currentTier, equalTo(expectedTier.id))

        val managementTier = managementTierDevRepository.findByIdPersonId(person.id)
        assertThat(managementTier.id.tierId, equalTo(expectedTier.id))
        assertThat(managementTier.tierChangeReasonId, equalTo(expectedReason.id))

        val contact = contactDevRepository.findByPersonId(person.id)
        assertThat(contact.type.code, equalTo(ContactTypeCode.TIER_UPDATE.code))

        verify(telemetryService).trackEvent(
            "TierUpdateSuccess",
            mapOf(
                "crn" to "A000002",
                "tier" to "D2",
                "calculationId" to "123e4567-e89b-12d3-a456-426614174000",
                "calculationDate" to ZonedDateTimeDeserializer.deserialize("2021-04-23T18:25:43.511Z").toString()
            )
        )
    }

    @Test
    @Order(2)
    fun `end-dates previous tier when feature flag is disabled`() {
        whenever(featureFlags.enabled("tier-to-delius-v3")).thenReturn(false)
        val notification = prepEvent("tier-update", wireMockServer.port()).withCrn("A000002")

        channelManager.getChannel(queueName).publishAndWait(notification)

        val expectedTier = referenceDataRepository.findByCodeAndSetName("UC2", "TIER")!!
        val person = personRepository.findByCrnAndSoftDeletedIsFalse(notification.message.personReference.findCrn()!!)!!
        assertThat(person.currentTier, equalTo(expectedTier.id))

        val managementTiers = managementTierDevRepository.findAllByIdPersonIdOrderByIdDateChanged(person.id)
        assertThat(
            managementTiers.first().endDate!!.toEpochSecond(),
            equalTo(ZonedDateTime.parse("2021-04-24T18:25:43.511Z").toEpochSecond())
        )
    }
}
