package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.Duration
import java.time.ZonedDateTime
import kotlin.jvm.optionals.getOrNull

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class AllocationMessagingIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @SpyBean
    lateinit var staffRepository: StaffRepository

    @Autowired
    lateinit var prisonManagerRepository: PrisonManagerRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Order(1)
    @Test
    fun `allocate first POM successfully`() {
        val notification = prepNotification(
            notification("new-allocation"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        val captor = argumentCaptor<Staff>()
        verify(staffRepository).save(captor.capture())
        assertThat(captor.firstValue.forename, equalTo("John"))
        assertThat(captor.firstValue.surname, equalTo("Smith"))

        val prisonManager =
            prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.DEFAULT.id, ZonedDateTime.now())
        assertThat(prisonManager?.allocationReason?.code, equalTo("AUT"))
        assertThat(prisonManager?.staff?.forename, equalTo("John"))
        assertThat(prisonManager?.staff?.surname, equalTo("Smith"))

        val contacts = contactRepository.findAll().filter { it.personId == PersonGenerator.DEFAULT.id }
        assertThat(contacts.map { it.type.code }, hasItems(ContactType.Code.POM_AUTO_ALLOCATION.value))

        verify(telemetryService).trackEvent(
            "POM Allocated",
            mapOf(
                "prisonId" to "SWI",
                "nomsId" to "A0123BY",
                "allocationDate" to "2023-05-09"
            )
        )
    }

    @Order(2)
    @Test
    fun `reallocate POM successfully`() {
        // add RO to existing pom to test RO behaviour
        val existingPom =
            prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.DEFAULT.id, ZonedDateTime.now())!!
        existingPom.responsibleOfficer =
            ResponsibleOfficer(existingPom.personId, existingPom, existingPom.date)
        prisonManagerRepository.save(existingPom)

        val notification = prepNotification(
            notification("pom-reallocated"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification, Duration.ofSeconds(180))

        val captor = argumentCaptor<Staff>()
        verify(staffRepository).save(captor.capture())
        assertThat(captor.firstValue.forename, equalTo("James"))
        assertThat(captor.firstValue.surname, equalTo("Brown"))

        val prisonManager =
            prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.DEFAULT.id, ZonedDateTime.now())
        assertThat(prisonManager?.allocationReason?.code, equalTo("INA"))
        assertThat(prisonManager?.staff?.forename, equalTo("James"))
        assertThat(prisonManager?.staff?.surname, equalTo("Brown"))
        assertNotNull(prisonManager?.responsibleOfficer)
        assertNull(prisonManager?.responsibleOfficer?.endDate)

        val previousPom = prisonManagerRepository.findById(existingPom.id).getOrNull()
        assertNotNull(previousPom?.endDate)
        assertNotNull(previousPom?.responsibleOfficer?.endDate)

        val contacts = contactRepository.findAll().filter { it.personId == PersonGenerator.DEFAULT.id }
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.POM_INTERNAL_ALLOCATION.value,
                ContactType.Code.RESPONSIBLE_OFFICER_CHANGE.value
            )
        )

        verify(telemetryService).trackEvent(
            "POM Allocated",
            mapOf(
                "prisonId" to "SWI",
                "nomsId" to "A0123BY",
                "allocationDate" to "2023-10-09"
            )
        )
    }
}
