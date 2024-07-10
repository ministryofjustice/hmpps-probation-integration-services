package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.data.entity.IapsPersonRepository
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.OasysAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.RegistrationRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.getByCrn
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
import uk.gov.justice.digital.hmpps.service.Risk
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.LocalDate

@SpringBootTest
internal class IntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var personRepository: PersonRepository

    @Autowired
    lateinit var registrationRepository: RegistrationRepository

    @Autowired
    lateinit var oasysAssessmentRepository: OasysAssessmentRepository

    @Autowired
    lateinit var iapsPersonRepository: IapsPersonRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    @MockBean
    lateinit var telemetryService: TelemetryService

    lateinit var transactionTemplate: TransactionTemplate

    @BeforeEach
    fun setUp() {
        transactionTemplate = TransactionTemplate(transactionManager)
    }

    @Test
    fun `a new assessment is created and existing registrations are deregistered`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced-${PersonGenerator.NO_RISK.crn}")
        val prevRegs =
            registrationRepository.findByPersonIdAndTypeFlagCode(
                PersonGenerator.NO_RISK.id,
                ReferenceDataGenerator.DEFAULT_FLAG.code
            )
        assertThat(prevRegs.size, equalTo(2))

        channelManager.getChannel(queueName).publishAndWait(prepNotification(message, wireMockServer.port()))

        val person = personRepository.getByCrn(PersonGenerator.NO_RISK.crn)
        assertNull(person.highestRiskColour)

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.DEFAULT_FLAG.code)
        assertTrue(registrations.isEmpty())

        val assessment = oasysAssessmentRepository.findAll().firstOrNull { it.person.id == person.id }
        assertThat(assessment?.court?.code, equalTo("CRT150"))
        assertThat(assessment?.offence?.code, equalTo("80400"))
        assertThat(assessment?.assessedBy, equalTo("John Smith"))
        assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-07")))
        assertThat(assessment?.totalScore, equalTo(76))
        assertThat(assessment?.initialSentencePlanDate, equalTo(LocalDate.of(2024, 2, 12)))
        assertThat(assessment?.sentencePlanReviewDate, equalTo(LocalDate.of(2024, 8, 12)))

        val contact = contactRepository.findAll()
            .single { it.person.id == person.id && it.type.code == ContactType.Code.OASYS_ASSESSMENT.value }
        assertThat(contact.externalReference, equalTo("urn:uk:gov:hmpps:oasys:assessment:${assessment?.oasysId}"))
    }

    @Test
    fun `an existing assessment is replaced, a new registration added and iaps is notified`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced-${PersonGenerator.LOW_RISK.crn}")
        val prevRegs =
            registrationRepository.findByPersonIdAndTypeFlagCode(
                PersonGenerator.LOW_RISK.id,
                ReferenceDataGenerator.DEFAULT_FLAG.code
            )
        assertThat(prevRegs.size, equalTo(1))

        channelManager.getChannel(queueName)
            .publishAndWait(prepNotification(message, wireMockServer.port()))

        val person = personRepository.getByCrn(PersonGenerator.LOW_RISK.crn)
        assertThat(person.highestRiskColour, equalTo("Green"))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.DEFAULT_FLAG.code)
        assertThat(registrations.size, equalTo(1))
        val reg = registrations.first()
        assertThat(reg.type.code, equalTo(RegistrationGenerator.TYPES[Risk.L.code]?.code))

        val assessment = oasysAssessmentRepository.findByOasysId("10096930")
        assertThat(assessment?.court?.code, equalTo("CRT150"))
        assertThat(assessment?.offence?.code, equalTo("80400"))
        assertThat(assessment?.assessedBy, equalTo("John Smith"))
        assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-07")))
        assertThat(assessment?.totalScore, equalTo(94))

        val scores = assessment?.sectionScores?.associate { it.id.level to it.score }!!
        assertThat(scores[3L], equalTo(1))
        assertThat(scores[4L], equalTo(2))
        assertThat(scores[6L], equalTo(3))
        assertThat(scores[7L], equalTo(4))
        assertThat(scores[8L], equalTo(5))
        assertThat(scores[9L], equalTo(6))
        assertThat(scores[11L], equalTo(7))
        assertThat(scores[12L], equalTo(8))

        val iaps = iapsPersonRepository.findAll().firstOrNull { it.personId == person.id }
        assertThat(iaps?.iapsFlag, equalTo(true))
    }

    @Test
    fun `a new assessment is created with sentence plan objectives, needs and actions`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced-${PersonGenerator.MEDIUM_RISK.crn}")

        channelManager.getChannel(queueName)
            .publishAndWait(prepNotification(message, wireMockServer.port()))

        val person = personRepository.getByCrn(PersonGenerator.MEDIUM_RISK.crn)
        assertThat(person.highestRiskColour, equalTo("Amber"))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.DEFAULT_FLAG.code)
        assertThat(registrations.size, equalTo(1))
        val reg = registrations.first()
        assertThat(reg.type.code, equalTo(RegistrationGenerator.TYPES[Risk.M.code]?.code))

        transactionTemplate.execute {
            val assessment = oasysAssessmentRepository.findByOasysId("100835871")
            assertThat(assessment?.court?.code, equalTo("LVRPCC"))
            assertThat(assessment?.offence?.code, equalTo("00857"))
            assertThat(assessment?.assessedBy, equalTo("LevelTwo CentralSupport"))
            assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-15")))
            assertThat(assessment?.totalScore, equalTo(88))

            val sentencePlans = assessment!!.sentencePlans
            assertThat(sentencePlans.size, equalTo(2))
            val first = sentencePlans.first()
            assertThat(
                first.objective,
                equalTo("Increased knowledge of physical/ psychological/ emotional self harm linked to drug use")
            )
            val needs = first.needs.sortedBy { it.id.level }
            assertThat(needs.first().need, equalTo("Risk to Public"))
            val actions = first.workSummaries.sortedBy { it.id.level }
            assertThat(actions.first().workSummary, equalTo("Drug counselling"))
            val texts = first.texts.sortedBy { it.id.level }
            assertThat(texts.first().text, equalTo("Frank will need to attend regularly"))
            assertThat(sentencePlans[1].objective, equalTo("Improve employment related skills"))
        }
    }

    @Test
    fun `a new assessment is created and an existing registration matches the current risk`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced-${PersonGenerator.HIGH_RISK.crn}")

        channelManager.getChannel(queueName)
            .publishAndWait(prepNotification(message, wireMockServer.port()))

        val person = personRepository.getByCrn(PersonGenerator.HIGH_RISK.crn)
        assertThat(person.highestRiskColour, equalTo("Orange"))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.DEFAULT_FLAG.code)
        assertThat(registrations.size, equalTo(1))
        val reg = registrations.first()
        assertThat(reg.type.code, equalTo(RegistrationGenerator.TYPES[Risk.H.code]?.code))

        val assessment = oasysAssessmentRepository.findByOasysId("10078385")
        assertThat(assessment?.court?.code, equalTo("LVRPCC"))
        assertThat(assessment?.offence?.code, equalTo("00857"))
        assertThat(assessment?.assessedBy, equalTo("LevelTwo CentralSupport"))
        assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-15")))
        assertThat(assessment?.totalScore, equalTo(108))
        val scores = assessment?.sectionScores?.associate { it.id.level to it.score }!!
        assertThat(scores[3L], equalTo(8))
        assertThat(scores[4L], equalTo(7))
        assertThat(scores[6L], equalTo(4))
        assertThat(scores[7L], equalTo(5))
        assertThat(scores[8L], equalTo(4))
        assertThat(scores[9L], equalTo(0))
        assertThat(scores[11L], equalTo(5))
        assertThat(scores[12L], equalTo(5))
    }

    @Test
    fun `an assessment without a cmsEventNumber is logged and ignored`() {
        val message =
            notification<HmppsDomainEvent>("assessment-summary-produced-${PersonGenerator.PERSON_NO_EVENT.crn}")

        channelManager.getChannel(queueName)
            .publishAndWait(prepNotification(message, wireMockServer.port()))

        verify(telemetryService, timeout(5000)).trackEvent(
            eq("AssessmentSummaryFailure"),
            ArgumentMatchers.anyMap(),
            ArgumentMatchers.anyMap()
        )
    }
}
