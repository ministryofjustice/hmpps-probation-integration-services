package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.repository.ContactDevRepository
import uk.gov.justice.digital.hmpps.data.repository.ManagementTierDevRepository
import uk.gov.justice.digital.hmpps.datetime.ZonedDateTimeDeserializer
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest {

    @Value("\${messaging.consumer.queue}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

    @Autowired
    private lateinit var referenceDataRepository: ReferenceDataRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var managementTierDevRepository: ManagementTierDevRepository

    @Autowired
    private lateinit var contactDevRepository: ContactDevRepository

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `updates a tier`() {
        val notification = prepMessage("tier-calculation", wireMockServer.port()).copy(
            attributes = MessageAttributes("tier.calculation.complete")
        )

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
                "crn" to "A000001",
                "tier" to "D2",
                "calculationDate" to ZonedDateTimeDeserializer.deserialize("2021-04-23T18:25:43.511Z").toString()
            )
        )
    }
}
