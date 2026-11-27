package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.contact
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.integration.EntityType
import uk.gov.justice.digital.hmpps.integration.ReferralStart
import uk.gov.justice.digital.hmpps.message.PersonIdentifier
import uk.gov.justice.digital.hmpps.message.PersonReference
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.repository.RequirementRepository
import uk.gov.justice.digital.hmpps.service.ComponentCommencementService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ComponentCommencementIntegrationTest @Autowired constructor(
    @Value("\${messaging.consumer.queue}") private val queueName: String,
    private val channelManager: HmppsChannelManager,
    private val wireMockServer: WireMockServer,
    private val contactRepository: ContactRepository,
    private val requirementRepository: RequirementRepository,
    private val licenceConditionRepository: LicenceConditionRepository,
    private val commencementService: ComponentCommencementService,
) {
    // Date from wiremock programme-started-mappings.json
    private val wireMockStartDate = LocalDate.of(2025, 4, 1)

    @Test
    fun `programme started message commences a requirement`() {
        val id = TestData.COMMENCEMENT_REQUIREMENT.id
        val event = prepMessage("programme-started-requirement", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(event)
        channelManager.getChannel(queueName).publishAndWait(event)

        val requirement = requirementRepository.findByIdOrNull(id)!!
        assertThat(requirement.commencementDate).isEqualTo(wireMockStartDate.atStartOfDay(EuropeLondon))
        assertThat(requirement.notes).isEqualTo(
            "Actual Start Date set to 01/04/2025 following notification from the Accredited Programmes – Intervention Service"
        )
        val contact = contactRepository.findByRequirementIdAndTypeCode(id, ContactType.ORDER_COMPONENT_COMMENCED)
        assertThat(contact).isNotNull
        assertThat(contact!!.date).isEqualTo(wireMockStartDate)
        assertThat(contact.staff.id).isEqualTo(TestData.STAFF.id)
        assertThat(contact.team.id).isEqualTo(TestData.TEAM.id)
    }

    @Test
    fun `programme started message commences a licence condition`() {
        val id = TestData.COMMENCEMENT_LICENCE_CONDITION.id
        val message = prepMessage("programme-started-licence-condition", wireMockServer.port())
        channelManager.getChannel(queueName).publishAndWait(message)

        assertThat(licenceConditionRepository.findByIdOrNull(id)!!.commencementDate)
            .isEqualTo(wireMockStartDate.atStartOfDay(EuropeLondon))
        val contact = contactRepository.findByLicenceConditionIdAndTypeCode(id, ContactType.ORDER_COMPONENT_COMMENCED)
        assertThat(contact).isNotNull
        assertThat(contact!!.date).isEqualTo(wireMockStartDate)
        assertThat(contact.staff.id).isEqualTo(TestData.STAFF.id)
    }

    @Test
    fun `already commenced requirement is unchanged when programme started message is received`() {
        val id = TestData.ALREADY_COMMENCED_REQUIREMENT.id
        val oldDate = wireMockStartDate.minusDays(7)
        val oldContact = contactRepository.save(
            TestData.ALREADY_COMMENCED_REQUIREMENT.contact(
                TestData.ORDER_COMPONENT_COMMENCED_CONTACT_TYPE, TestData.STAFF_2, TestData.TEAM_2,
                TestData.PROVIDER, oldDate, LocalTime.of(9, 30), LocalTime.of(10, 30)
            )
        )

        val message = prepMessage("programme-started-requirement", wireMockServer.port())
        val event = message.copy(
            message = message.message.copy(
                detailUrl = message.message.detailUrl!!.replace(
                    "00000000-0000-0000-0000-000000000001",
                    "00000000-0000-0000-0000-000000000002"
                ),
                personReference = PersonReference(
                    listOf(PersonIdentifier("CRN", TestData.ALREADY_COMMENCED_REQUIREMENT_PERSON.crn))
                ),
            )
        )
        channelManager.getChannel(queueName).publishAndWait(event)

        val updated = requirementRepository.findByIdOrNull(id)!!
        assertThat(updated.commencementDate).isEqualTo(oldDate.atStartOfDay(EuropeLondon))
        assertThat(updated.notes).isEqualTo("Existing notes")
        val contact = contactRepository.findByIdOrNull(oldContact.id)!!
        assertThat(contact.date).isEqualTo(oldDate)
        assertThat(contact.startTime).isEqualTo(oldDate.atTime(9, 30).atZone(EuropeLondon))
        assertThat(contact.endTime).isEqualTo(oldDate.atTime(10, 30).atZone(EuropeLondon))
        assertThat(contact.staff.id).isEqualTo(TestData.STAFF_2.id)
        assertThat(contact.team.id).isEqualTo(TestData.TEAM_2.id)
        assertThat(contactRepository.findAll().count {
            it.requirement?.id == id && it.type.code == ContactType.ORDER_COMPONENT_COMMENCED
        }).isEqualTo(1)
    }

    @Test
    fun `component must belong to correct CRN`() {
        val component = TestData.COMMENCEMENT_REQUIREMENT
        val beforeCommencement = requirementRepository.findByIdOrNull(component.id)!!.commencementDate
        assertThatThrownBy { commencementService.commence("ZZZZZZZ", detail(component.id)) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("CRN and component do not match")
        assertThat(requirementRepository.findByIdOrNull(component.id)!!.commencementDate).isEqualTo(beforeCommencement)
    }

    private fun detail(componentId: Long) = ReferralStart(
        licReqId = componentId.toString(),
        licReqStartedAt = LocalDateTime.parse("2025-04-01T09:30:00"),
        sourcedFromEntityType = EntityType.REQUIREMENT,
    )
}
