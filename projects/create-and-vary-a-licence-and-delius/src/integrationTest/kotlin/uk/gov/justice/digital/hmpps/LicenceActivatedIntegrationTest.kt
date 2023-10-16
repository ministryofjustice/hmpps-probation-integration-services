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
import uk.gov.justice.digital.hmpps.integrations.delius.manager.entity.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMapping.Companion.BESPOKE_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.CvlMapping.Companion.STANDARD_CATEGORY_CODE
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import uk.gov.justice.digital.hmpps.service.ActionResult
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService

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

    @Test
    fun `add licence conditions`() {
        val sentence = SentenceGenerator.SENTENCE_CREATE_LC
        val person = sentence.event.person

        val notification = prepMessage(
            ResourceLoader.event("licence-activated-L453621"),
            wireMockServer.port()
        )

        channelManager.getChannel(queueName).publishAndWait(notification)

        val telemetryProperties = mapOf(
            "crn" to "L453621",
            "eventNumber" to "1",
            "releaseDate" to "2023-10-13",
            "standardConditions" to "2",
            "additionalConditions" to "2",
            "bespokeConditions" to "2"
        )

        verify(telemetryService).trackEvent(ActionResult.Type.StandardLicenceConditionAdded.name, telemetryProperties)
        verify(telemetryService).trackEvent(
            ActionResult.Type.AdditionalLicenceConditionsAdded.name,
            telemetryProperties
        )

        val conditions = lcr.findByDisposalId(sentence.id)
        assertThat(conditions.size, equalTo(4))

        val com = pmr.findByPersonCrn(person.crn)!!
        conditions.forEach {
            val lcm = lcmr.findByLicenceConditionId(it.id)
            assertThat(lcm?.providerId, equalTo(com.provider.id))
            assertThat(lcm?.teamId, equalTo(com.team.id))
            assertThat(lcm?.staffId, equalTo(com.staff.id))
        }

        val standard = conditions.first { it.mainCategory.code == STANDARD_CATEGORY_CODE }
        assertThat(
            standard.notes,
            equalTo(
                """
            |A Standard Condition
            |Another Standard Condition
                """.trimMargin()
            )
        )

        val bespoke = conditions.first { it.mainCategory.code == BESPOKE_CATEGORY_CODE }
        assertThat(
            bespoke.notes,
            equalTo(
                """
            |First Bespoke Condition
            |Second Bespoke Condition
                """.trimMargin()
            )
        )

        val additional =
            conditions.filter { it.mainCategory.code !in listOf(STANDARD_CATEGORY_CODE, BESPOKE_CATEGORY_CODE) }
        assertThat(
            additional.map { it.notes },
            containsInAnyOrder("Additional Licence Condition One", "Additional Licence Condition Two")
        )
    }
}
