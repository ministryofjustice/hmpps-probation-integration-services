package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.repository.ManagementTierDevRepository
import uk.gov.justice.digital.hmpps.data.repository.PersonWithV3TierDevRepository
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.NotificationExtensions.withCrn
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}")
    private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val referenceDataRepository: ReferenceDataRepository,
    private val personRepository: PersonRepository,
    private val managementTierDevRepository: ManagementTierDevRepository,
    private val contactRepository: ContactRepository,
    private val wireMockServer: WireMockServer,
    private val personWithV3TierDevRepository: PersonWithV3TierDevRepository,
    @MockitoBean private val featureFlags: FeatureFlags,
    @MockitoBean private val telemetryService: TelemetryService,
) {

    @Test
    fun `uses v2 tier for current tier, management tier and contact when phase flags are disabled`() {
        givenFeatureFlags(phase1Enabled = false, phase2Enabled = false)
        val initial = publishInitial("A000001")
        assertThat(initial.currentTier(), equalTo("UD2"))
        assertThat(initial.v3Tier(), nullValue())
        assertThat(initial.managementTiers().map { it.id.tierId }, hasItems(tierId("UD2")))
        assertThat(initial.contacts().size, equalTo(1))

        val updated = publishUpdate("A000001")
        assertThat(updated.currentTier(), equalTo("UC2"))
        assertThat(updated.v3Tier(), nullValue())
        assertThat(initial.managementTiers().map { it.id.tierId }, hasItems(tierId("UD2"), tierId("UC2")))
        assertThat(updated.contacts().size, equalTo(2))
    }

    @Test
    fun `updates hidden v3 tier and uses v2 tier when phase 1 is enabled`() {
        givenFeatureFlags(phase1Enabled = true, phase2Enabled = false)
        val initial = publishInitial("A000002")
        assertThat(initial.currentTier(), equalTo("UD2"))
        assertThat(initial.v3Tier(), equalTo("SPB"))
        assertThat(initial.managementTiers().map { it.id.tierId }, hasItems(tierId("UD2")))
        assertThat(initial.contacts().size, equalTo(1))

        val updated = publishUpdate("A000002")
        assertThat(updated.currentTier(), equalTo("UC2"))
        assertThat(initial.v3Tier(), equalTo("SPA"))
        assertThat(updated.managementTiers().map { it.id.tierId }, hasItems(tierId("UD2"), tierId("UC2")))
        assertThat(updated.contacts().size, equalTo(2))
    }

    @Test
    fun `uses v3 tier for current tier, management tier and contact when phase 2 is enabled`() {
        givenFeatureFlags(phase1Enabled = false, phase2Enabled = true)
        val initial = publishInitial("A000003")
        assertThat(initial.currentTier(), equalTo("SPB"))
        assertThat(initial.v3Tier(), nullValue())
        assertThat(initial.managementTiers().map { it.id.tierId }, hasItems(tierId("SPB")))
        assertThat(initial.contacts().size, equalTo(1))

        val updated = publishUpdate("A000003")
        assertThat(updated.currentTier(), equalTo("SPA"))
        assertThat(initial.v3Tier(), nullValue())
        assertThat(updated.managementTiers().map { it.id.tierId }, hasItems(tierId("SPB"), tierId("SPA")))
        assertThat(updated.contacts().size, equalTo(2))
    }

    @Test
    fun `updates hidden v3 tier and uses v3 tier when both phases are enabled`() {
        givenFeatureFlags(phase1Enabled = true, phase2Enabled = true)
        val initial = publishInitial("A000004")
        assertThat(initial.currentTier(), equalTo("SPB"))
        assertThat(initial.v3Tier(), equalTo("SPB"))
        assertThat(initial.managementTiers().map { it.id.tierId }, hasItems(tierId("SPB")))
        assertThat(initial.contacts().size, equalTo(1))

        val updated = publishUpdate("A000004")
        assertThat(updated.currentTier(), equalTo("SPA"))
        assertThat(initial.v3Tier(), equalTo("SPA"))
        assertThat(updated.managementTiers().map { it.id.tierId }, hasItems(tierId("SPB"), tierId("SPA")))
        assertThat(updated.contacts().size, equalTo(2))
    }

    private fun publishInitial(crn: String): Person {
        channelManager.getChannel(queueName)
            .publishAndWait(prepEvent("tier-calculation", wireMockServer.port()).withCrn(crn))
        verify(telemetryService).trackEvent(
            eq("TierUpdateSuccess"),
            argThat { this["crn"] == crn && this["calculationId"] == INITIAL_CALCULATION_ID },
            any()
        )
        return personRepository.findByCrnAndSoftDeletedIsFalse(crn)!!
    }

    private fun publishUpdate(crn: String): Person {
        channelManager.getChannel(queueName)
            .publishAndWait(prepEvent("tier-update", wireMockServer.port()).withCrn(crn))
        verify(telemetryService).trackEvent(
            eq("TierUpdateSuccess"),
            argThat { this["crn"] == crn && this["calculationId"] == UPDATED_CALCULATION_ID },
            any()
        )
        return personRepository.findByCrnAndSoftDeletedIsFalse(crn)!!
    }

    private fun Person.contacts() = contactRepository.findAll().filter { it.person.id == id }
    private fun Person.managementTiers() = managementTierDevRepository.findAllByIdPersonIdOrderByIdDateChanged(id)

    private fun Person.v3Tier(): String? =
        personWithV3TierDevRepository.findByCrn(crn)?.v3TierId?.let { referenceDataRepository.findByIdOrNull(it) }?.code

    private fun Person.currentTier(): String? = currentTier?.let { referenceDataRepository.findByIdOrNull(it) }?.code

    private fun tierId(code: String) = referenceDataRepository.findByCodeAndSetName(code, "TIER")!!.id

    private fun givenFeatureFlags(phase1Enabled: Boolean, phase2Enabled: Boolean) {
        whenever(featureFlags.enabled("tier-v3-delius-phase-1")).thenReturn(phase1Enabled)
        whenever(featureFlags.enabled("tier-v3-delius-phase-2")).thenReturn(phase2Enabled)
    }

    private companion object {
        const val INITIAL_CALCULATION_ID = "123e4567-e89b-12d3-a456-426614174000"
        const val UPDATED_CALCULATION_ID = "123e4567-e89b-12d3-a456-426614174001"
    }
}
