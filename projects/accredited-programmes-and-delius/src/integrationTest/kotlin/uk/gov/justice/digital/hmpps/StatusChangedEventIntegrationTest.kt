package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.integration.EntityType
import uk.gov.justice.digital.hmpps.integration.StatusInfo
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.service.StatusChangeService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.util.*

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class StatusChangedEventIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val wireMockServer: WireMockServer,
    private val contactRepository: ContactRepository,
    private val statusChangeService: StatusChangeService,
    @MockitoSpyBean private val telemetryService: TelemetryService,
) {

    @Test
    fun `breach status contact created`() {
        val eventName = "status-changed-breach"
        val event = prepMessage(eventName, wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", "${TestData.LICENCE_CONDITIONS.first().id}")
                )
            )
        )

        val contact = contactRepository.findAll().lastOrNull {
            it.person.id == TestData.PERSON.id &&
                it.type.code == StatusInfo.Status.BREACH.contactTypeCode
        }
        assertThat(contact).isNotNull
        assertThat(contact!!.licenceCondition?.id).isEqualTo(TestData.LICENCE_CONDITIONS.first().id)
        assertThat(contact.notes).isEqualTo("Some notes about the breach of the LC")
        assertThat(contact.externalReference).isNotNull
    }

    @Test
    fun `on programme status contact created`() {
        val eventName = "status-changed-on-programme"
        val event = prepMessage(eventName, wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", "${TestData.REQUIREMENTS.first().id}")
                )
            )
        )

        val contact = contactRepository.findAll().lastOrNull {
            it.person.id == TestData.PERSON.id &&
                it.type.code == StatusInfo.Status.ON_PROGRAMME.contactTypeCode
        }
        assertThat(contact).isNotNull
        assertThat(contact!!.requirement?.id).isEqualTo(TestData.REQUIREMENTS.first().id)
        assertThat(contact.description).isEqualTo("Some description")
        assertThat(contact.notes).isEqualTo("Some notes about the being on the programme")
        assertThat(contact.externalReference).isNotNull
    }

    @Test
    fun `missing licence condition is ignored and tracked`() {
        assertMissingComponentIgnoredAndTracked("status-changed-breach", StatusInfo.Status.BREACH)
    }

    @Test
    fun `missing requirement is ignored and tracked`() {
        assertMissingComponentIgnoredAndTracked("status-changed-on-programme", StatusInfo.Status.ON_PROGRAMME)
    }

    @Test
    fun `missing component returns false`() {
        assertThat(
            statusChangeService.statusChanged(
                UUID.randomUUID(),
                crn = TestData.PERSON.crn,
                occurredAt = ZonedDateTime.now(),
                info = StatusInfo(
                    newStatus = StatusInfo.Status.BREACH,
                    sourcedFromEntityType = EntityType.LICENCE_CONDITION,
                    sourcedFromEntityId = -1,
                    notes = "Some notes",
                    description = "Some description",
                    username = "testuser"
                ),
            )
        ).isFalse()
    }

    @Test
    fun `wrong CRN throws`() {
        assertThatThrownBy {
            statusChangeService.statusChanged(
                UUID.randomUUID(),
                crn = "Z99999",
                occurredAt = ZonedDateTime.now(),
                info = StatusInfo(
                    newStatus = StatusInfo.Status.ON_PROGRAMME,
                    sourcedFromEntityType = EntityType.REQUIREMENT,
                    sourcedFromEntityId = TestData.REQUIREMENTS.first().id,
                    notes = "Some notes",
                    description = "Some description",
                    username = "testuser"
                ),
            )
        }.isInstanceOf(IllegalArgumentException::class.java).hasMessage("CRN and component do not match")
    }

    private fun assertMissingComponentIgnoredAndTracked(eventName: String, status: StatusInfo.Status) {
        reset(telemetryService)
        val missingId = -1L
        val event = prepMessage(eventName, wireMockServer.port())
        val contactCountBefore = contactRepository.count()

        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", missingId.toString())
                )
            )
        )

        assertThat(contactRepository.count()).isEqualTo(contactCountBefore)
        verify(telemetryService).trackEvent(
            "StatusChangedComponentNotFound",
            mapOf(
                "crn" to TestData.PERSON.crn,
                "componentId" to missingId.toString(),
                "status" to status.toString(),
                "username" to "testuser",
            )
        )
        verify(telemetryService, never()).trackEvent(
            "StatusChanged",
            mapOf(
                "crn" to TestData.PERSON.crn,
                "componentId" to missingId.toString(),
                "status" to status.toString(),
                "username" to "testuser",
            )
        )
    }
}