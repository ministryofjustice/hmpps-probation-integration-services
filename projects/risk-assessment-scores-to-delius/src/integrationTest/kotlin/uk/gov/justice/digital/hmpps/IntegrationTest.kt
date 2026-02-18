package uk.gov.justice.digital.hmpps

import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.*
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.flags.FeatureFlags
import uk.gov.justice.digital.hmpps.integrations.delius.RiskAssessmentService
import uk.gov.justice.digital.hmpps.integrations.delius.RiskScoreService
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.OGRSAssessmentRepository
import uk.gov.justice.digital.hmpps.message.MessageAttributes
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.Ogrs4Score
import uk.gov.justice.digital.hmpps.messaging.telemetryProperties
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
internal class IntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val ogrsAssessmentRepository: OGRSAssessmentRepository,
    private val contactRepository: ContactRepository,
    private val riskAssessmentService: RiskAssessmentService
) {
    @MockitoBean
    private lateinit var telemetryService: TelemetryService

    @MockitoBean
    private lateinit var featureFlags: FeatureFlags

    @MockitoBean
    private lateinit var riskScoreService: RiskScoreService

    @Test
    fun `successfully update RSR scores feature flag true`() {
        whenever(featureFlags.enabled("delius-ogrs4-support")).thenReturn(true)
        doNothing().whenever(riskScoreService).updateRsrAndOspScores(
            any(), anyOrNull(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )

        val notification = Notification(
            message = MessageGenerator.RSR_SCORES_DETERMINED_V4,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("RsrScoresUpdated", notification.message.telemetryProperties())
    }

    @Test
    fun `successfully update RSR scores when band full word(s)`() {
        whenever(featureFlags.enabled("delius-ogrs4-support")).thenReturn(true)
        doNothing().whenever(riskScoreService).updateRsrAndOspScores(
            any(), anyOrNull(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )

        val notification = Notification(
            message = MessageGenerator.RSR_SCORES_DETERMINED_V4_LONG_BAND,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("RsrScoresUpdated", notification.message.telemetryProperties())
    }

    @Test
    fun `successfully update RSR scores feature flag false`() {
        whenever(featureFlags.enabled("delius-ogrs4-support")).thenReturn(false)
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
        assertThat(ogrsAssessmentRepository.findAll().size).isEqualTo(1)
        assertThat(ogrsAssessmentRepository.findAll()[0].ogrs3Score1).isEqualTo(4)
        assertThat(ogrsAssessmentRepository.findAll()[0].ogrs3Score2).isEqualTo(8)

        // Verify that the Contact has been created
        assertThat(contactRepository.findAll().size).isEqualTo(1)
        assertThat(contactRepository.findAll()[0].notes).contains("OGRS3: 4% within 1 year and 8% within 2 years.")
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
        assertThat(ogrsAssessmentRepository.findAll()).hasSize(1)
        assertThat(ogrsAssessmentRepository.findAll()[0].ogrs3Score1).isEqualTo(5)
        assertThat(ogrsAssessmentRepository.findAll()[0].ogrs3Score2).isEqualTo(9)

        // Verify that the Contact has been created
        assertThat(contactRepository.findAll()).hasSize(2)
        MatcherAssert.assertThat(
            contactRepository.findAll()[1].notes,
            Matchers.containsString("OGRS3: 5% within 1 year and 9% within 2 years.")
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
                Ogrs4Score(
                    1, 2, null, null,
                    null, null, null, null
                )
            )
        }

        val res2 = CompletableFuture.runAsync {
            riskAssessmentService.addOrUpdateRiskAssessment(
                crn,
                1,
                ZonedDateTime.now().minusMonths(1),
                Ogrs4Score(
                    1, 2, null, null,
                    null, null, null, null
                )
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
        assertThat(assessment?.event?.number).isEqualTo("3")
        assertThat(assessment?.ogrs3Score1).isEqualTo(5)
        assertThat(assessment?.ogrs3Score2).isEqualTo(7)
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
        assertThat(assessment?.ogrs3Score1).isEqualTo(4)
        assertThat(assessment?.ogrs3Score2).isEqualTo(6)
    }

    @Test
    @Order(6)
    fun `successfully add OGRS assessment with new OGRS4 fields`() {
        val person = PersonGenerator.OGRS4
        val notification = Notification(
            message = MessageGenerator.OGRS_SCORES_DETERMINED_OGRS4,
            attributes = MessageAttributes("risk-assessment.scores.determined")
        )
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent("AddOrUpdateRiskAssessment", notification.message.telemetryProperties())

        val assessment = ogrsAssessmentRepository.findAll().find { it.event.person.crn == person.crn }
        MatcherAssert.assertThat(assessment, Matchers.notNullValue())
        assertThat(assessment!!.ogrs3Score1).isEqualTo(43)
        assertThat(assessment.ogrs3Score2).isEqualTo(60)
        assertThat(assessment.arpStaticDynamic).isEqualTo("S")
        assertThat(assessment.arpScore).isEqualTo(54.21)
        assertThat(assessment.arpBand).isEqualTo("M")

        val contact = contactRepository.findAll().find { it.event?.person?.crn == person.crn }
        assertThat(contact!!.notes).contains("OGRS3: 43% within 1 year and 60% within 2 years.")
        assertThat(contact.notes).contains("All Reoffending Predictor (ARP): Static ARP score is 54.21% - Medium")
    }
}
