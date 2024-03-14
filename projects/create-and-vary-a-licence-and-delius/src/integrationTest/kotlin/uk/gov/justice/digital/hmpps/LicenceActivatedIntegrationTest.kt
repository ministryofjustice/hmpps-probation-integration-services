package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.data.generator.SentenceGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategory.Companion.BESPOKE_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionCategory.Companion.STANDARD_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.TransferReason
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.service.ActionResult
import uk.gov.justice.digital.hmpps.service.CONDITION_PREFIX
import uk.gov.justice.digital.hmpps.service.LIMITED_PREFIX
import uk.gov.justice.digital.hmpps.service.STANDARD_PREFIX
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.Duration
import java.time.ZonedDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LicenceActivatedIntegrationTest {
    @Value("\${messaging.consumer.queue}")
    lateinit var queueName: String

    @Autowired
    lateinit var channelManager: HmppsChannelManager

    @Autowired
    lateinit var wireMockServer: WireMockServer

    @MockBean
    lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var lcr: LicenceConditionRepository

    @Autowired
    lateinit var lcmr: LicenceConditionManagerRepository

    @Autowired
    lateinit var pmr: PersonManagerRepository

    @Autowired
    lateinit var contactRepository: ContactRepository

    @Test
    fun `add licence conditions`() {
        val sentence = SentenceGenerator.SENTENCE_CREATE_LC
        val person = sentence.disposal.event.person

        val notification = prepMessage(
            ResourceLoader.event("licence-activated-L453621"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification, timeout = Duration.ofMinutes(2))

        val telemetryProperties = mapOf(
            "crn" to "L453621",
            "eventNumber" to "1",
            "startDate" to "2024-02-05",
            "standardConditions" to "2",
            "additionalConditions" to "4",
            "bespokeConditions" to "2"
        )

        verify(telemetryService).trackEvent(ActionResult.Type.StandardLicenceConditionAdded.name, telemetryProperties)
        verify(telemetryService).trackEvent(
            ActionResult.Type.AdditionalLicenceConditionAdded.name,
            telemetryProperties
        )
        verify(telemetryService).trackEvent(
            ActionResult.Type.BespokeLicenceConditionAdded.name,
            telemetryProperties
        )

        val conditions = lcr.findByDisposalId(sentence.disposal.id)
        assertThat(conditions.size, equalTo(7))

        val com = pmr.findByPersonCrn(person.crn)!!
        conditions.forEach {
            val lcm = lcmr.findByLicenceConditionId(it.id)
            assertThat(lcm?.providerId, equalTo(com.provider.id))
            assertThat(lcm?.teamId, equalTo(com.team.id))
            assertThat(lcm?.staffId, equalTo(com.staff.id))
            assertThat(lcm?.transferReason?.code, equalTo(TransferReason.DEFAULT_CODE))
            assertThat(lcm?.allocationReason?.code, equalTo(ReferenceData.INITIAL_ALLOCATION_CODE))
        }

        val standard = conditions.first { it.mainCategory.code == STANDARD_CATEGORY_CODE }
        assertThat(
            standard.notes,
            equalTo(
                """
            |${STANDARD_PREFIX}
            |A Standard Condition
            |Another Standard Condition
                """.trimMargin()
            )
        )

        val bespoke = conditions.filter { it.mainCategory.code == BESPOKE_CATEGORY_CODE }
        assertThat(
            bespoke.map { it.notes },
            containsInAnyOrder(
                "First Bespoke Condition".prefixed(),
                "Second Bespoke Condition".prefixed()
            )
        )
        assertThat(
            bespoke.map { it.cvlText },
            containsInAnyOrder(
                "First Bespoke Condition",
                "Second Bespoke Condition"
            )
        )

        val additional =
            conditions.filter { it.mainCategory.code !in listOf(STANDARD_CATEGORY_CODE, BESPOKE_CATEGORY_CODE) }
        assertThat(
            additional.map { it.notes },
            containsInAnyOrder(
                "Additional Licence Condition One".prefixed(),
                "Additional Licence Condition Two".prefixed(),
                "Additional Licence Condition Electronic Monitoring".prefixed(),
                LIMITED_PREFIX
            )
        )
        assertThat(
            additional.mapNotNull { it.cvlText },
            containsInAnyOrder(
                "Additional Licence Condition One",
                "Additional Licence Condition Two",
                "Additional Licence Condition Electronic Monitoring"
            )
        )

        val lpop = contactRepository.findAll().filter { it.personId == person.id && it.type.code == ContactType.LPOP }
        assertThat(lpop.size, equalTo(1))
        assertThat(
            lpop.first().notes,
            equalTo(
                "Delius has been updated with licence conditions entered in the Create and Vary a licence service."
            )
        )
        val occurredAt = ZonedDateTime.parse("2022-12-04T10:42:43+00:00")
        assertThat(lpop.first().date, equalTo(occurredAt.toLocalDate()))
        assertThat(lpop.first().startTime!!, isCloseTo(occurredAt))

        // send again to confirm licence conditions are not duplicated
        channelManager.getChannel(queueName).publishAndWait(notification)
        verify(telemetryService).trackEvent(ActionResult.Type.NoChangeToLicenceConditions.name, telemetryProperties)
    }

    private fun String.prefixed(prefix: String = CONDITION_PREFIX): String = prefix + System.lineSeparator() + this
}
