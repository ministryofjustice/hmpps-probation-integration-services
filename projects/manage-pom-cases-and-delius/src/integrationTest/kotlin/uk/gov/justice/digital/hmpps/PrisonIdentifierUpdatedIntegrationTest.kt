package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.repository.PrisonStaffRepository
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@SpringBootTest
internal class PrisonIdentifierUpdatedIntegrationTest {
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

    @MockitoSpyBean
    lateinit var staffRepository: PrisonStaffRepository

    @MockitoSpyBean
    lateinit var prisonManagerRepository: PrisonManagerRepository

    @MockitoSpyBean
    lateinit var contactRepository: ContactRepository

    @Test
    fun `fetch and allocate pom when noms number added`() {
        val notification = prepNotification(
            notification("prison-identifier-added"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        val prisonManager =
            prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.DEFAULT.id, ZonedDateTime.now())
        assertThat(prisonManager?.allocationReason?.code, equalTo("AUT"))
        assertThat(prisonManager?.staff?.forename, equalTo("John"))
        assertThat(prisonManager?.staff?.surname, equalTo("Smith"))
        assertThat(prisonManager?.emailAddress, equalTo("john.smith@justice.gov.uk"))

        val contacts = contactRepository.findAll().filter { it.personId == PersonGenerator.DEFAULT.id }
        assertThat(contacts.map { it.type.code }, hasItems(ContactType.Code.POM_AUTO_ALLOCATION.value))

        verify(telemetryService).trackEvent(
            "PomAllocated",
            mapOf(
                "prisonId" to "SWI",
                "nomsId" to "A0123BY",
                "allocationDate" to "24/07/2025 10:00:00"
            )
        )
    }

    @Test
    fun `fetch and allocate pom when noms number updated`() {
        val notification = prepNotification(
            notification("prison-identifier-updated"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        val prisonManager =
            prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.HANDOVER.id, ZonedDateTime.now())
        assertThat(prisonManager?.allocationReason?.code, equalTo("AUT"))
        assertThat(prisonManager?.staff?.forename, equalTo("James"))
        assertThat(prisonManager?.staff?.surname, equalTo("Brown"))
        assertThat(prisonManager?.emailAddress, equalTo("james.brown@justice.gov.uk"))

        val contacts = contactRepository.findAll().filter { it.personId == PersonGenerator.HANDOVER.id }
        assertThat(contacts.map { it.type.code }, hasItems(ContactType.Code.POM_AUTO_ALLOCATION.value))

        verify(telemetryService).trackEvent(
            "PomAllocated",
            mapOf(
                "prisonId" to "SWI",
                "nomsId" to "A1024BY",
                "allocationDate" to "24/07/2025 10:00:00"
            )
        )
    }
}