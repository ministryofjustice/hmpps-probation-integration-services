package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator
import uk.gov.justice.digital.hmpps.data.repository.IapsRequirementRepository
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManager
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.EventType
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

@ActiveProfiles("integration-test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AllocateRequirementIntegrationTest {

    @Value("\${spring.jms.template.default-destination}")
    private lateinit var queueName: String

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @Autowired
    private lateinit var wireMockServer: WireMockServer

    @Autowired
    private lateinit var requirementManagerRepository: RequirementManagerRepository

    @Autowired
    private lateinit var iapsRequirementRepository: IapsRequirementRepository

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `allocate new requirement manager`() {
        val requirement = RequirementGenerator.DEFAULT
        val existingManager = requirementManagerRepository.findByIdOrNull(RequirementManagerGenerator.DEFAULT.id)
            ?: throw NotFoundException("Requirement Manager Not Found")
        val originalRmCount = requirementManagerRepository.findAll().count { it.requirementId == requirement.id }

        allocateAndValidate(existingManager, requirement, originalRmCount)
    }

    @Test
    fun `allocate historic requirement manager`() {
        val requirement = RequirementGenerator.DEFAULT

        val firstRm = requirementManagerRepository.save(
            requirementManagerRepository.findByIdOrNull(RequirementManagerGenerator.DEFAULT.id)?.apply {
                endDate = ZonedDateTime.now().minusDays(1)
            }!!
        )
        val secondRm =
            requirementManagerRepository.save(RequirementManagerGenerator.generate(startDateTime = firstRm.endDate!!))

        val originalRmCount = requirementManagerRepository.findAll().count { it.requirementId == requirement.id }

        allocateAndValidate(firstRm, requirement, originalRmCount)

        val insertedRm =
            requirementManagerRepository.findActiveManagerAtDate(requirement.id, ZonedDateTime.now().minusDays(2))
        assertThat(
            insertedRm?.endDate?.truncatedTo(ChronoUnit.SECONDS)?.withZoneSameInstant(EuropeLondon),
            equalTo(secondRm.startDate.truncatedTo(ChronoUnit.SECONDS).withZoneSameInstant(EuropeLondon))
        )
    }

    private fun allocateAndValidate(
        existingRm: RequirementManager,
        requirement: Requirement,
        originalRmCount: Int,
    ) {
        val allocationEvent = prepMessage("requirement-allocation-message", wireMockServer.port())
        jmsTemplate.convertSendAndWait(queueName, allocationEvent)

        verify(telemetryService).trackEvent(
            eq("${EventType.REQUIREMENT_ALLOCATED}_RECEIVED"),
            eq(
                mapOf(
                    "eventType" to allocationEvent.eventType.value,
                    "detailUrl" to allocationEvent.detailUrl,
                    "CRN" to allocationEvent.personReference.findCrn()!!
                )
            ),
            ArgumentMatchers.anyMap()
        )

        val allocationDetail = ResourceLoader.allocationBody("get-requirement-allocation-body")

        val oldRm = requirementManagerRepository.findById(existingRm.id).orElseThrow()
        assertThat(
            oldRm.endDate?.withZoneSameInstant(EuropeLondon),
            equalTo(allocationDetail.createdDate.withZoneSameInstant(EuropeLondon))
        )

        val updatedRmCount = requirementManagerRepository.findAll().count { it.requirementId == requirement.id }
        assertThat(originalRmCount + 1, equalTo(updatedRmCount))

        assert(iapsRequirementRepository.findById(requirement.id).isPresent)
    }
}
