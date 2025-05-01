package uk.gov.justice.digital.hmpps

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.tomakehurst.wiremock.WireMockServer
import jakarta.persistence.EntityManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.hamcrest.core.IsEqual.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import uk.gov.justice.digital.hmpps.data.entity.IapsPersonRepository
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.RegistrationGenerator
import uk.gov.justice.digital.hmpps.datetime.DeliusDateFormatter
import uk.gov.justice.digital.hmpps.datetime.toDeliusDate
import uk.gov.justice.digital.hmpps.enum.RiskLevel
import uk.gov.justice.digital.hmpps.enum.RiskOfSeriousHarmType
import uk.gov.justice.digital.hmpps.enum.RiskType
import uk.gov.justice.digital.hmpps.integrations.delius.assessment.entity.OasysAssessmentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.domainevent.entity.DomainEventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.*
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import uk.gov.justice.digital.hmpps.message.Notification
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.messaging.crn
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader.notification
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

    @Autowired
    lateinit var domainEventRepository: DomainEventRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var entityManager: EntityManager

    @MockitoBean
    lateinit var telemetryService: TelemetryService

    lateinit var transactionTemplate: TransactionTemplate

    @BeforeEach
    fun setUp() {
        transactionTemplate = TransactionTemplate(transactionManager)
    }

    @Test
    fun `assessment with no risk information does not change registrations, but does add assessment and contact`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(PersonGenerator.NO_ROSH.crn)
        val prevRegs =
            registrationRepository.findByPersonIdAndTypeFlagCode(
                PersonGenerator.NO_ROSH.id,
                ReferenceDataGenerator.ROSH_FLAG.code
            )
        assertThat(prevRegs.size, equalTo(2))
        val prevRiskColour = personRepository.getByCrn(PersonGenerator.NO_ROSH.crn).highestRiskColour
        assertThat(prevRiskColour, equalTo("Green"))

        channelManager.getChannel(queueName).publishAndWait(message)

        val person = personRepository.getByCrn(PersonGenerator.NO_ROSH.crn)
        assertThat(person.highestRiskColour, equalTo(prevRiskColour))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.ROSH_FLAG.code)
        assertThat(registrations.size, equalTo(2))
        assertThat(registrations.map { it.id }, equalTo(prevRegs.map { it.id }))

        val assessment = oasysAssessmentRepository.findAll().firstOrNull { it.person.id == person.id }
        assertThat(assessment?.court?.code, equalTo("CRT150"))
        assertThat(assessment?.offence?.code, equalTo("80400"))
        assertThat(assessment?.assessedBy, equalTo("John Smith"))
        assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-07")))
        assertThat(assessment?.totalScore, equalTo(76))
        assertThat(assessment?.initialSentencePlanDate, equalTo(LocalDate.of(2024, 2, 12)))
        assertThat(assessment?.sentencePlanReviewDate, equalTo(LocalDate.of(2024, 8, 12)))
        assertThat(assessment?.status?.code, equalTo("LI"))

        val contact = contactRepository.findAll()
            .single { it.person.id == person.id && it.type.code == ContactType.Code.OASYS_ASSESSMENT_LOCKED_INCOMPLETE.value }
        assertThat(contact.date, equalTo(assessment?.date))
        assertThat(contact.externalReference, equalTo("urn:uk:gov:hmpps:oasys:assessment:${assessment?.oasysId}"))
    }

    @Test
    fun `an existing assessment is replaced, a new registration is added, existing registrations are deregistered, and iaps is notified`() {
        val message =
            notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(PersonGenerator.LOW_ROSH.crn)
        val prevRegs =
            registrationRepository.findByPersonIdAndTypeFlagCode(
                PersonGenerator.LOW_ROSH.id,
                ReferenceDataGenerator.ROSH_FLAG.code
            )
        assertThat(prevRegs.size, equalTo(2))
        assertThat(
            prevRegs.map { it.type.code },
            equalTo(listOf(RiskOfSeriousHarmType.M.code, RiskOfSeriousHarmType.H.code))
        )

        val prevAssessment = oasysAssessmentRepository.findByOasysId(PersonGenerator.LOW_ROSH.oasysId())!!
        val prevAssessmentContact = prevAssessment.contact
        assertThat(
            prevAssessmentContact.externalReference,
            equalTo("urn:uk:gov:hmpps:oasys:assessment:${prevAssessment.oasysId}")
        )

        channelManager.getChannel(queueName).publishAndWait(message)

        val person = personRepository.getByCrn(PersonGenerator.LOW_ROSH.crn)
        assertThat(person.highestRiskColour, equalTo("Green"))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.ROSH_FLAG.code)
        assertThat(registrations.size, equalTo(1))
        val reg = registrations.first()
        assertThat(reg.type.code, equalTo(RiskOfSeriousHarmType.L.code))

        val assessment = oasysAssessmentRepository.findByOasysId(PersonGenerator.LOW_ROSH.oasysId())!!
        assertThat(assessment.court?.code, equalTo("CRT150"))
        assertThat(assessment.offence?.code, equalTo("80400"))
        assertThat(assessment.assessedBy, equalTo("John Smith"))
        assertThat(assessment.date, equalTo(LocalDate.parse("2023-12-07")))
        assertThat(assessment.totalScore, equalTo(94))
        assertThat(assessment.status?.code, equalTo("C"))

        val contact = assessment.contact
        assertThat(contact.date, equalTo(assessment.date))
        assertThat(contact.type.code, equalTo(ContactType.Code.OASYS_ASSESSMENT_COMPLETE.value))
        assertThat(contact.externalReference, equalTo("urn:uk:gov:hmpps:oasys:assessment:${assessment.oasysId}"))
        assertThat(contact.copyToVisor, equalTo(true))

        val prevContact = contactRepository.findByIdOrNull(prevAssessmentContact.id)!!
        assertThat(prevContact.externalReference, equalTo(null))

        val scores = assessment.sectionScores.associate { it.id.level to it.score }
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
        val message =
            notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(PersonGenerator.MEDIUM_ROSH.crn)

        channelManager.getChannel(queueName).publishAndWait(message)

        val person = personRepository.getByCrn(PersonGenerator.MEDIUM_ROSH.crn)
        assertThat(person.highestRiskColour, equalTo("Amber"))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.ROSH_FLAG.code)
        assertThat(registrations.size, equalTo(1))
        val reg = registrations.first()
        assertThat(reg.type.code, equalTo(RegistrationGenerator.TYPES[RiskOfSeriousHarmType.M.code]?.code))

        transactionTemplate.execute {
            val assessment = oasysAssessmentRepository.findByOasysId(PersonGenerator.MEDIUM_ROSH.oasysId())
            assertThat(assessment?.court?.code, equalTo("LVRPCC"))
            assertThat(assessment?.offence?.code, equalTo("00857"))
            assertThat(assessment?.assessedBy, equalTo("R. L. Name"))
            assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-15")))
            assertThat(assessment?.totalScore, equalTo(88))
            assertThat(assessment?.status?.code, equalTo("C"))

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
        val message =
            notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(PersonGenerator.HIGH_ROSH.crn)

        channelManager.getChannel(queueName).publishAndWait(message)

        val person = personRepository.getByCrn(PersonGenerator.HIGH_ROSH.crn)
        assertThat(person.highestRiskColour, equalTo("Orange"))

        val registrations =
            registrationRepository.findByPersonIdAndTypeFlagCode(person.id, ReferenceDataGenerator.ROSH_FLAG.code)
        assertThat(registrations.size, equalTo(1))
        val reg = registrations.first()
        assertThat(reg.type.code, equalTo(RegistrationGenerator.TYPES[RiskOfSeriousHarmType.H.code]?.code))

        val assessment = oasysAssessmentRepository.findByOasysId(PersonGenerator.HIGH_ROSH.oasysId())
        assertThat(assessment?.court?.code, equalTo("LVRPCC"))
        assertThat(assessment?.offence?.code, equalTo("00857"))
        assertThat(assessment?.assessedBy, equalTo("LevelTwo CentralSupport"))
        assertThat(assessment?.date, equalTo(LocalDate.parse("2023-12-15")))
        assertThat(assessment?.totalScore, equalTo(108))
        assertThat(assessment?.status?.code, equalTo("C"))

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
    fun `an assessment with a cmsEventNumber that is soft deleted is logged and ignored`() {
        val message =
            notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(PersonGenerator.PERSON_SOFT_DELETED_EVENT.crn)

        channelManager.getChannel(queueName).publishAndWait(message)

        verify(telemetryService, timeout(5000)).trackEvent(
            eq("AssessmentSummaryFailureReport"),
            check {
                assertThat(it["reason"], Matchers.equalTo("Event with number of 1 not found"))
            },
            anyMap()
        )
    }

    @Test
    fun `an assessment with a crn that is not found is logged and ignored`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced").withCrn("Z999999")

        channelManager.getChannel(queueName).publishAndWait(message)

        verify(telemetryService, timeout(5000)).trackEvent(
            eq("AssessmentSummaryFailureReport"),
            check {
                assertThat(it["reason"], Matchers.equalTo("Person with crn of Z999999 not found"))
            },
            anyMap()
        )
    }

    @Test
    fun `an assessment that is not found is logged and ignored`() {
        val message = notification<HmppsDomainEvent>("assessment-summary-produced").withCrn("Y999999")

        channelManager.getChannel(queueName).publishAndWait(message)

        verify(telemetryService, timeout(5000)).trackEvent(
            eq("AssessmentSummaryFailureReport"),
            check {
                assertThat(it["reason"], Matchers.equalTo("No assessment in OASys"))
            },
            anyMap()
        )
    }

    @Test
    fun `event number is inferred for prison assessments`() {
        val message =
            notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(PersonGenerator.PRISON_ASSESSMENT.crn)

        channelManager.getChannel(queueName).publishAndWait(message)

        verify(telemetryService, timeout(5000)).trackEvent(eq("AssessmentSummarySuccess"), anyMap(), anyMap())
        val assessment = oasysAssessmentRepository.findByOasysId(PersonGenerator.PRISON_ASSESSMENT.oasysId())
        assertThat(assessment?.eventNumber, equalTo("1"))
    }

    @Test
    fun `an assessment without a cmsEventNumber for a person without a matching event is logged and ignored`() {
        val message =
            notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(PersonGenerator.PERSON_NO_EVENT.crn)

        channelManager.getChannel(queueName).publishAndWait(message)

        verify(telemetryService, timeout(5000)).trackEvent(
            eq("AssessmentSummaryFailureReport"),
            check { assertThat(it["reason"], Matchers.equalTo("No single active custodial event")) },
            anyMap()
        )
    }

    @Test
    fun `an assessment with risks identified creates individual registrations`() {
        val person = personRepository.getByCrn(PersonGenerator.NO_EXISTING_RISKS.crn)
        val message = notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(person.crn)
        val previousRegistrations = registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.CHILDREN.code)
        assertThat(previousRegistrations.size, equalTo(0))

        channelManager.getChannel(queueName).publishAndWait(message)

        val registrations = registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.CHILDREN.code)
        assertThat(registrations, hasSize(1))
        val registration = registrations.single()
        assertThat(registration.type.code, equalTo(RiskType.CHILDREN.code))
        assertThat(registration.level?.code, equalTo(RiskLevel.H.code))
        assertThat(
            registration.notes,
            equalTo(
                """
                    The OASys assessment of Review on 07/12/2023 identified the Risk to children to be H.
                    
                    *R10.1 Who is at risk*
                    Known adults - Joe Bloggs
                    
                    *R10.2 What is the nature of the risk*
                    Physical harm - Physical assault, shown willingness to use a weapon
                """.trimIndent()
            )
        )

        assertThat(registration.reviews, hasSize(1))
        val review = registration.reviews.single()
        assertThat(
            review.contact.notes?.trim(), equalTo(
                """
                    Type: Safeguarding - Risk to children
                    Next Review Date: ${DeliusDateFormatter.format(LocalDate.now().plusMonths(6))}
                """.trimIndent()
            )
        )
        assertThat(review.date, equalTo(LocalDate.now().plusMonths(6)))
    }

    @Test
    fun `existing risks are updated and new reviews are created`() {
        val person = personRepository.getByCrn(PersonGenerator.EXISTING_RISKS.crn)
        val message = notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(person.crn)

        val riskToChildrenBefore =
            registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.CHILDREN.code).single()
        assertThat(riskToChildrenBefore.level?.code, equalTo(RiskLevel.H.code))
        assertThat(riskToChildrenBefore.reviews, hasSize(1))
        assertThat(riskToChildrenBefore.reviews[0].completed, equalTo(false))
        val riskToPrisonerBefore = registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.PRISONER.code)
        assertThat(riskToPrisonerBefore, hasSize(0))
        val altRiskToPrisonerBefore =
            registrationRepository.findByPersonIdAndTypeCode(person.id, RegistrationGenerator.ALT_TYPE.code).single()
        assertThat(altRiskToPrisonerBefore.level?.code, nullValue())
        val riskToStaffBefore =
            registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.STAFF.code).single()
        assertThat(riskToStaffBefore.level?.code, equalTo(RiskLevel.V.code))
        val riskToAdultBefore =
            registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.KNOWN_ADULT.code).single()
        assertThat(riskToAdultBefore.level?.code, equalTo(RiskLevel.M.code))
        assertThat(riskToAdultBefore.reviews, hasSize(1))
        val riskToAdultReviewId = riskToAdultBefore.reviews.single().id
        val riskToPublicBefore =
            registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.PUBLIC.code).single()
        assertThat(riskToPublicBefore.level?.code, equalTo(RiskLevel.M.code))

        channelManager.getChannel(queueName).publishAndWait(message)

        val domainEvents = domainEventRepository.findAllForCrn(PersonGenerator.EXISTING_RISKS.crn)

        // Unchanged value from OASys - completes existing review and adds a new review
        val riskToChildren =
            registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.CHILDREN.code).single()
        assertThat(riskToChildren.level?.code, equalTo(RiskLevel.H.code))
        assertThat(riskToChildren.reviews, hasSize(2))
        assertThat(riskToChildren.nextReviewDate, equalTo(LocalDate.now().plusMonths(6)))
        val completedReview = riskToChildren.reviews.single { it.completed }
        assertThat(completedReview.date, equalTo(LocalDate.now()))
        assertThat(completedReview.reviewDue, equalTo(LocalDate.now().plusMonths(6)))
        """
            existing notes
            The OASys assessment of Review on 07/12/2023 identified the Risk to children to be H.

            *R10.1 Who is at risk*
            Known adults - Joe Bloggs

            *R10.2 What is the nature of the risk*
            Physical harm - Physical assault, shown willingness to use a weapon
        """.trimIndent().let { assertThat(completedReview.notes?.trim(), equalTo(it)) }
        """
            existing notes
            The OASys assessment of Review on 07/12/2023 identified the Risk to children to be H.

            See review for more details
        """.trimIndent().let { assertThat(completedReview.contact.notes?.trim(), equalTo(it)) }
        val newReview = riskToChildren.reviews.single { !it.completed }
        assertThat(newReview.date, equalTo(LocalDate.now().plusMonths(6)))
        assertThat(newReview.reviewDue, nullValue())
        """
            Type: Safeguarding - Risk to children
            Next Review Date: ${LocalDate.now().plusMonths(6).toDeliusDate()}
        """.trimIndent().let {
            assertThat(newReview.notes?.trim(), equalTo(it))
            assertThat(newReview.contact.notes?.trim(), equalTo(it))
        }
        assertThat(domainEvents.ofType(RiskType.CHILDREN), hasSize(0))

        // No existing registration - add new
        val riskToPrisoner =
            registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.PRISONER.code).single()
        assertThat(riskToPrisoner.level?.code, equalTo(RiskLevel.M.code))
        assertThat(riskToPrisoner.reviews, hasSize(1))
        assertThat(domainEvents.ofType(RiskType.PRISONER), hasSize(1))
        // removes any in duplicate group
        val altRiskToPrisoner =
            registrationRepository.findByPersonIdAndTypeCode(person.id, RegistrationGenerator.ALT_TYPE.code)
        assertThat(altRiskToPrisoner, empty())

        // null from OASys - no change
        val riskToStaff = registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.STAFF.code).single()
        assertThat(riskToStaff.level?.code, equalTo(RiskLevel.V.code))
        assertThat(riskToStaff.reviews, hasSize(1))
        assertThat(domainEvents.ofType(RiskType.STAFF), hasSize(0))

        // Low from OASys - remove existing
        val riskToAdult = registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.KNOWN_ADULT.code)
        assertThat(riskToAdult, hasSize(0))
        assertThat(domainEvents.ofType(RiskType.KNOWN_ADULT), hasSize(1))
        val rtaReview = entityManager.find(RegistrationReview::class.java, riskToAdultReviewId)
        assertThat(rtaReview.completed, equalTo(true))

        // Changed level - remove existing and add new
        val riskToPublic = registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.PUBLIC.code).single()
        assertThat(riskToPublic.level?.code, equalTo(RiskLevel.V.code))
        assertThat(riskToPublic.reviews, hasSize(1))
        """
            The OASys assessment of Review on 07/12/2023 identified the Risk to public to have increased to V.

            *R10.1 Who is at risk*
            Known adults - Joe Bloggs

            *R10.2 What is the nature of the risk*
            Physical harm - Physical assault, shown willingness to use a weapon
        """.trimIndent().let { assertThat(riskToPublic.notes, equalTo(it)) }
        """
            
            The OASys assessment of Review on 07/12/2023 identified the Risk to public to have increased to V.

            See Register for more details
        """.trimIndent().let { assertThat(riskToPublic.contact.notes, equalTo(it)) }
        assertThat(domainEvents.ofType(RiskType.PUBLIC), hasSize(2))
        assertThat(
            domainEvents.ofType(RiskType.PUBLIC).map { it.eventType },
            hasItems("probation-case.registration.added", "probation-case.registration.deregistered")
        )
    }

    @Test
    fun `level is added in-place to existing risk registrations with no level`() {
        val person = personRepository.getByCrn(PersonGenerator.EXISTING_RISKS_WITHOUT_LEVEL.crn)
        val message = notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(person.crn)

        channelManager.getChannel(queueName).publishAndWait(message)

        val riskToChildren =
            registrationRepository.findByPersonIdAndTypeCode(person.id, RiskType.CHILDREN.code).single()
        assertThat(riskToChildren.level?.code, equalTo(RiskLevel.H.code))
        assertThat(riskToChildren.reviews, hasSize(2))

        val domainEvents = domainEventRepository.findAllForCrn(PersonGenerator.EXISTING_RISKS_WITHOUT_LEVEL.crn)
        assertThat(domainEvents.ofType(RiskType.CHILDREN), hasSize(0))
    }

    @Test
    fun `locked incomplete assessments result in contact with a different type and do not change registrations`() {
        val person = PersonGenerator.LOCKED_INCOMPLETE
        val message = notification<HmppsDomainEvent>("assessment-summary-produced").withCrn(person.crn)

        channelManager.getChannel(queueName).publishAndWait(message)

        val assessment = oasysAssessmentRepository.findAll().firstOrNull { it.person.id == person.id }
        val contact = contactRepository.findAll()
            .single { it.person.id == person.id && it.type.code == ContactType.Code.OASYS_ASSESSMENT_LOCKED_INCOMPLETE.value }
        assertThat(contact.date, equalTo(assessment?.date))
        assertThat(contact.externalReference, equalTo("urn:uk:gov:hmpps:oasys:assessment:${assessment?.oasysId}"))

        val registrationDomainEvents = domainEventRepository.findAll()
            .map { objectMapper.readValue<HmppsDomainEvent>(it.messageBody) }
            .filter { it.crn() == person.crn }
        assertThat(registrationDomainEvents, empty())
    }

    private fun Notification<HmppsDomainEvent>.withCrn(crn: String): Notification<HmppsDomainEvent> {
        val oasysId = crn.drop(1).toInt()
        return this.copy(
            message = this.message.copy(
                detailUrl = "http://localhost:${wireMockServer.port()}/eor/oasys/ass/asssumm/$crn/ALLOW/${oasysId}/COMPLETE",
                personReference = PersonReference(listOf(PersonIdentifier("CRN", crn)))
            )
        )
    }

    private fun Person.oasysId() = crn.drop(1).toInt().toString()

    private fun List<HmppsDomainEvent>.ofType(type: RiskType) =
        filter { it.additionalInformation["registerTypeCode"] == type.code }

    private fun DomainEventRepository.findAllForCrn(crn: String) = findAll()
        .map { objectMapper.readValue<HmppsDomainEvent>(it.messageBody) }
        .filter { it.crn() == crn }
}
