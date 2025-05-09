package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.integrations.delius.RiskAssessmentService
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.NotificationHandler
import uk.gov.justice.digital.hmpps.messaging.OgrsScore
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    private lateinit var channelManager: HmppsChannelManager

    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var handler: NotificationHandler<HmppsDomainEvent>

    @Autowired
    private lateinit var ogrsAssessmentRepository: OGRSAssessmentRepository

    @Autowired
    private lateinit var contactRepository: ContactRepository

    @Autowired
    private lateinit var riskAssessmentService: RiskAssessmentService

    @Test
    fun `successfully update RSR scores`() {
        val notification = Notification(
            message = MessageGenerator.RSR_SCORES_DETERMINED,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("RsrScoresUpdated", notification.message.telemetryProperties())
    }

    @Test
    fun `handles old OSP scores`() {
        val notification = Notification(
            message = MessageGenerator.RSR_SCORES_DETERMINED_WITHOUT_OSPIIC_OSPDC,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("RsrScoresUpdated", notification.message.telemetryProperties())
    }

    @Test
    fun `handles new OSP scores`() {
        val notification = Notification(
            message = MessageGenerator.RSR_SCORES_DETERMINED_WITH_OSPII_OSPDC,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("RsrScoresUpdated", notification.message.telemetryProperties())
    }

    @Test
    @Order(1)
    fun `successfully add OGRS assessment`() {
        val notification = Notification(
            message = MessageGenerator.OGRS_SCORES_DETERMINED,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("AddOrUpdateRiskAssessment", notification.message.telemetryProperties())

        // Verify that the OGRS assessment has been created
        ogrsAssessmentRepository.findAll().size
        MatcherAssert.assertThat(ogrsAssessmentRepository.findAll().size, Matchers.equalTo(1))
        MatcherAssert.assertThat(ogrsAssessmentRepository.findAll()[0].ogrs3Score1, Matchers.equalTo(4))
        MatcherAssert.assertThat(ogrsAssessmentRepository.findAll()[0].ogrs3Score2, Matchers.equalTo(8))

        // Verify that the Contact has been created
        MatcherAssert.assertThat(contactRepository.findAll().size, Matchers.equalTo(1))
        MatcherAssert.assertThat(
            contactRepository.findAll()[0].notes,
            Matchers.containsString("Reconviction calculation is 4% within one year and 8% within 2 years.")
        )
    }

    @Test
    @Order(2)
    fun `successfully update OGRS assessment`() {
        val notification = Notification(
            message = MessageGenerator.OGRS_SCORES_DETERMINED_UPDATE,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("AddOrUpdateRiskAssessment", notification.message.telemetryProperties())

        // Verify that the OGRS assessment has been created
        ogrsAssessmentRepository.findAll().size
        MatcherAssert.assertThat(ogrsAssessmentRepository.findAll().size, Matchers.equalTo(1))
        MatcherAssert.assertThat(ogrsAssessmentRepository.findAll()[0].ogrs3Score1, Matchers.equalTo(5))
        MatcherAssert.assertThat(ogrsAssessmentRepository.findAll()[0].ogrs3Score2, Matchers.equalTo(9))

        // Verify that the Contact has been created
        MatcherAssert.assertThat(contactRepository.findAll().size, Matchers.equalTo(2))
        MatcherAssert.assertThat(
            contactRepository.findAll()[1].notes,
            Matchers.containsString("Reconviction calculation is 5% within one year and 9% within 2 years.")
        )
    }

    @Test
    @Order(3)
    fun `locking test`() {
        val crn = PersonGenerator.DEFAULT.crn
        val res1 = CompletableFuture.runAsync {
            riskAssessmentService.addOrUpdateRiskAssessment(
                crn,
                1,
                ZonedDateTime.now().minusMonths(1),
                OgrsScore(1, 2)
            )
        }

        val res2 = CompletableFuture.runAsync {
            riskAssessmentService.addOrUpdateRiskAssessment(
                crn,
                1,
                ZonedDateTime.now().minusMonths(1),
                OgrsScore(1, 2)
            )
        }

        val ce = assertThrows<CompletionException> {
            CompletableFuture.allOf(res1, res2).join()
        }

        assertThat(ce.cause is ConflictException)
    }

    @Test
    @Order(4)
    fun `successfully create OGRS assessment when event number is null`() {
        val person = PersonGenerator.NULL_EVENT_PROCESSING
        val notification = Notification(
            message = MessageGenerator.OGRS_SCORES_NULL_EVENT,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("AddOrUpdateRiskAssessment", notification.message.telemetryProperties())

        val assessment = ogrsAssessmentRepository.findAll().find { it.event.person.crn == person.crn }
        MatcherAssert.assertThat(assessment?.event?.number, Matchers.equalTo("3"))
        MatcherAssert.assertThat(assessment?.ogrs3Score1, Matchers.equalTo(5))
        MatcherAssert.assertThat(assessment?.ogrs3Score2, Matchers.equalTo(7))
    }

    @Test
    @Order(5)
    fun `successfully create OGRS assessment on a merged crn`() {
        val person = PersonGenerator.MERGED_TO
        val notification = Notification(
            message = MessageGenerator.OGRS_SCORES_MERGED,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("AddOrUpdateRiskAssessment", notification.message.telemetryProperties())

        val assessment = ogrsAssessmentRepository.findAll().find { it.event.person.crn == person.crn }
        MatcherAssert.assertThat(assessment?.ogrs3Score1, Matchers.equalTo(4))
        MatcherAssert.assertThat(assessment?.ogrs3Score2, Matchers.equalTo(6))
    }
}
