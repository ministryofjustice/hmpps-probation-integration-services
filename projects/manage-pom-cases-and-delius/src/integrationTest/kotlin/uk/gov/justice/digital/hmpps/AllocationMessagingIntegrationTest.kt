package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.StaffRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime

@SpringBootTest
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

    @Test
    fun `allocate new POM successfully`() {
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

        verify(telemetryService).trackEvent(
            "POM Allocated",
            mapOf(
                "prisonId" to "SWI",
                "nomsId" to "A0123BY",
                "allocationDate" to "2023-05-09"
            )
        )
    }
}
