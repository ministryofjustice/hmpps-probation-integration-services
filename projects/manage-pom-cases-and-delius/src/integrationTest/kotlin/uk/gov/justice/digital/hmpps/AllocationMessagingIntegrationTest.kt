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
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.entity.PrisonStaff
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.repository.PrisonStaffRepository
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
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

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    @MockitoSpyBean
    lateinit var staffRepository: PrisonStaffRepository

    @MockitoSpyBean
    lateinit var prisonManagerRepository: PrisonManagerRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var staffRepo: StaffRepository

    @Order(1)
    @Test
    fun `no change if not yet ready to allocate`() {
        val notification = prepNotification(
            notification("not-yet-allocation"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(prisonManagerRepository, never()).save(any())
        verify(telemetryService).trackEvent(
            "NotReadyToAllocate",
            mapOf(
                "nomsId" to "A0123BY",
                "allocationDate" to "09/05/2023 14:25:19"
            )
        )
    }

    @Order(2)
    @Test
    fun `allocate first POM successfully`() {
        val notification = prepNotification(
            notification("new-allocation"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        val captor = argumentCaptor<PrisonStaff>()
        verify(staffRepository).save(captor.capture())
        assertThat(captor.firstValue.forename, equalTo("John"))
        assertThat(captor.firstValue.surname, equalTo("Smith"))

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
                "allocationDate" to "09/05/2023 14:25:19"
            )
        )
    }

    @Order(3)
    @Test
    fun `reallocate POM successfully`() {
        // add RO to existing pom to test RO behaviour
        val existingPom =
            prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.DEFAULT.id, ZonedDateTime.now())!!
        existingPom.makeResponsibleOfficer()
        prisonManagerRepository.save(existingPom)

        val notification = prepNotification(
            notification("pom-reallocated"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        val captor = argumentCaptor<PrisonStaff>()
        verify(staffRepository).save(captor.capture())
        assertThat(captor.firstValue.forename, equalTo("James"))
        assertThat(captor.firstValue.surname, equalTo("Brown"))

        val prisonManager =
            prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.DEFAULT.id, ZonedDateTime.now())
        assertThat(prisonManager?.allocationReason?.code, equalTo("INA"))
        assertThat(prisonManager?.staff?.forename, equalTo("James"))
        assertThat(prisonManager?.staff?.surname, equalTo("Brown"))
        assertThat(prisonManager?.emailAddress, equalTo("james.brown@justice.gov.uk"))
        assertNotNull(prisonManager?.responsibleOfficer())
        assertNull(prisonManager?.responsibleOfficer()?.endDate)

        val previousPom = prisonManagerRepository.findByIdOrNull(existingPom.id)
        assertNotNull(previousPom?.endDate)
        previousPom?.responsibleOfficers?.forEach { assertNotNull(it.endDate) }
        assertNull(previousPom?.responsibleOfficer())

        val contacts = contactRepository.findAll().filter { it.personId == PersonGenerator.DEFAULT.id }
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.POM_INTERNAL_ALLOCATION.value,
                ContactType.Code.RESPONSIBLE_OFFICER_CHANGE.value
            )
        )

        verify(telemetryService).trackEvent(
            "PomAllocated",
            mapOf(
                "prisonId" to "SWI",
                "nomsId" to "A0123BY",
                "allocationDate" to "09/10/2023 14:25:19"
            )
        )
    }

    @Order(4)
    @Test
    fun `deallocate POM successfully`() {
        staffRepo.save(ProviderGenerator.UNALLOCATED_STAFF)

        val existingPom =
            prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.DEFAULT.id, ZonedDateTime.now())!!

        val notification = prepNotification(
            notification("deallocation"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        verify(staffRepository, never()).save(any())

        val prisonManager =
            prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.DEFAULT.id, ZonedDateTime.now())
        assertThat(prisonManager?.allocationReason?.code, equalTo("AUT"))
        assertThat(prisonManager?.staff?.code, equalTo(ProviderGenerator.UNALLOCATED_STAFF.code))
        assertThat(prisonManager?.staff?.forename, equalTo(ProviderGenerator.UNALLOCATED_STAFF.forename))
        assertThat(prisonManager?.staff?.surname, equalTo(ProviderGenerator.UNALLOCATED_STAFF.surname))

        val previousPom = prisonManagerRepository.findById(existingPom.id).getOrNull()
        assertNotNull(previousPom?.endDate)
        previousPom?.responsibleOfficers?.forEach { assertNotNull(it.endDate) }
        assertNull(previousPom?.responsibleOfficer())

        val contacts = contactRepository.findAll().filter { it.personId == PersonGenerator.DEFAULT.id }
        assertThat(
            contacts.map { it.type.code },
            hasItems(
                ContactType.Code.POM_AUTO_ALLOCATION.value,
                ContactType.Code.RESPONSIBLE_OFFICER_CHANGE.value
            )
        )

        verify(telemetryService).trackEvent(
            "PomDeallocated",
            mapOf(
                "nomsId" to "A0123BY",
                "allocationDate" to "10/10/2023 15:25:19"
            )
        )
    }
}
