package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.OasysAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

@SpringBootTest
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var registrationRepository: RegistrationRepository

    @Autowired
    lateinit var oasysAssessmentRepository: OasysAssessmentRepository

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Test
    fun `message is logged to telemetry`() {
        val person = PersonGenerator.NO_RISK
        val message = notification<HmppsDomainEvent>("assessment-summary-produced-${person.crn}")
        val prevRegs =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.DEFAULT_FLAG.code)
        assertThat(prevRegs.size, equalTo(2))

        channelManager.getChannel(queueName).publishAndWait(prepNotification(message, wireMockServer.port()))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.DEFAULT_FLAG.code)
        assertTrue(registrations.isEmpty())

        val assessment = oasysAssessmentRepository.findAll().firstOrNull { it.person.id == person.id }
        assertNotNull(assessment)
    }
}
