package uk.gov.justice.digital.hmpps

import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.data.repository.findByIdOrNull
import uk.gov.justice.digital.hmpps.data.RequirementTransferRepository
import uk.gov.justice.digital.hmpps.data.TestData
import uk.gov.justice.digital.hmpps.data.generator.ContactGenerator.contact
import uk.gov.justice.digital.hmpps.datetime.EuropeLondon
import uk.gov.justice.digital.hmpps.datetime.toDeliusDate
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.contact.ContactType
import uk.gov.justice.digital.hmpps.integration.StatusInfo.Status.PROGRAMME_COMPLETE
import uk.gov.justice.digital.hmpps.messaging.HmppsChannelManager
import uk.gov.justice.digital.hmpps.repository.ContactRepository
import uk.gov.justice.digital.hmpps.repository.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.repository.RejectedTransferDiaryRepository
import uk.gov.justice.digital.hmpps.repository.RequirementRepository
import uk.gov.justice.digital.hmpps.test.Assertions.assertNotNull
import java.time.ZonedDateTime

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ComponentTerminationIntegrationTest @Autowired constructor(
//    @MockitoSpyBean val telemetryService: TelemetryService, // FIXME dirtying the Spring context messes up the test data due to the DataLoader not correctly reinitialising
    @Value("\${messaging.consumer.queue}") val queueName: String,
    val channelManager: HmppsChannelManager,
    val wireMockServer: WireMockServer,
    val contactRepository: ContactRepository,
    val requirementRepository: RequirementRepository,
    val licenceConditionRepository: LicenceConditionRepository,
    val rejectedTransferDiaryRepository: RejectedTransferDiaryRepository,
    val requirementTransferRepository: RequirementTransferRepository,
) {
    @Test
    fun `requirement terminated when programme completed`() {
        val event = prepMessage("status-changed-programme-complete-requirement", wireMockServer.port())
        val terminationDate = event.message.occurredAt

        // Create a future appointment contact
        val futureAppointment = contactRepository.save(
            TestData.TERMINATION_REQUIREMENTS[0].contact(
                TestData.APPOINTMENT_CONTACT_TYPE,
                terminationDate.toLocalDate().plusDays(7),
                TestData.STAFF,
                TestData.TEAM,
                TestData.PROVIDER
            )
        )

        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", "${TestData.TERMINATION_REQUIREMENTS[0].id}")
                )
            )
        )

        val contacts =
            contactRepository.findAll().filter { it.requirement?.id == TestData.TERMINATION_REQUIREMENTS[0].id }

        // Verify status contact was created
        val statusContact =
            assertNotNull(contacts.singleOrNull { it.type.code == PROGRAMME_COMPLETE.contactTypeCode })
        assertThat(statusContact.notes).isEqualTo("Programme completed successfully")
        assertThat(statusContact.externalReference).startsWith("urn:uk:gov:hmpps:accredited-programmes-service:")

        // Verify termination contact was created
        val terminationContact =
            assertNotNull(contacts.firstOrNull { it.type.code == ContactType.COMPONENT_TERMINATED })
        assertThat(terminationContact.notes).isEqualTo("Requirement terminated on ${terminationDate.toDeliusDate()} with termination reason of \"Description of REQUIREMENT TERMINATION REASON\" following notification from the Accredited Programmes – Intervention Service")
        assertThat(terminationContact.staff.id).isEqualTo(TestData.STAFF.id)
        assertThat(terminationContact.team.id).isEqualTo(TestData.TEAM.id)

        // Verify requirement was terminated
        val requirement = assertNotNull(requirementRepository.findByIdOrNull(TestData.TERMINATION_REQUIREMENTS[0].id))
        assertThat(requirement.terminationDate).isEqualTo(terminationDate)
        assertThat(requirement.terminationReason?.code).isEqualTo(ReferenceData.REQUIREMENT_COMPLETED.code)
        assertThat(requirement.pendingTransfer).isFalse()

        // Verify future appointment was deleted
        val deletedContact = contactRepository.findById(futureAppointment.id)
        assertThat(deletedContact).isEmpty

        // Verify telemetry
//        verify(telemetryService).trackEvent(
//            "ComponentTerminated", mapOf(
//                "type" to "Requirement",
//                "id" to TestData.TERMINATION_REQUIREMENTS[0].id.toString(),
//                "crn" to "A000003",
//                "startDate" to "2025-01-01T12:00Z[Europe/London]",
//                "commencementDate" to null,
//                "terminationDate" to "2025-06-01T14:30+01:00[Europe/London]"
//            )
//        )
    }

    @Test
    fun `licence condition terminated when programme completed`() {
        val event = prepMessage("status-changed-programme-complete-licence-condition", wireMockServer.port())
        val terminationDate = event.message.occurredAt

        // Create a future appointment contact
        val futureAppointment = contactRepository.save(
            TestData.TERMINATION_LICENCE_CONDITION.contact(
                TestData.APPOINTMENT_CONTACT_TYPE,
                terminationDate.toLocalDate().plusDays(7),
                TestData.STAFF,
                TestData.TEAM,
                TestData.PROVIDER
            )
        )

        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", "${TestData.TERMINATION_LICENCE_CONDITION.id}")
                )
            )
        )

        val contacts =
            contactRepository.findAll().filter { it.licenceCondition?.id == TestData.TERMINATION_LICENCE_CONDITION.id }

        // Verify status contact was created
        val statusContact =
            assertNotNull(contacts.singleOrNull { it.type.code == PROGRAMME_COMPLETE.contactTypeCode })
        assertThat(statusContact.notes).isEqualTo("Programme completed successfully")
        assertThat(statusContact.externalReference).startsWith("urn:uk:gov:hmpps:accredited-programmes-service:")

        // Verify termination contact was created
        val terminationContact =
            assertNotNull(contacts.firstOrNull { it.type.code == ContactType.COMPONENT_TERMINATED })
        assertThat(terminationContact.notes).isEqualTo("Licence Condition terminated on ${terminationDate.toDeliusDate()} with termination reason of \"Description of LICENCE CONDITION TERMINATION REASON\" following notification from the Accredited Programmes – Intervention Service")
        assertThat(terminationContact.staff.id).isEqualTo(TestData.STAFF.id)
        assertThat(terminationContact.team.id).isEqualTo(TestData.TEAM.id)

        // Verify requirement was terminated
        val requirement =
            assertNotNull(licenceConditionRepository.findByIdOrNull(TestData.TERMINATION_LICENCE_CONDITION.id))
        assertThat(requirement.terminationDate).isEqualTo(terminationDate)
        assertThat(requirement.terminationReason?.code).isEqualTo(ReferenceData.LICENCE_CONDITION_COMPLETED.code)
        assertThat(requirement.pendingTransfer).isFalse()

        // Verify future appointment was deleted
        val deletedContact = contactRepository.findById(futureAppointment.id)
        assertThat(deletedContact).isEmpty

        // Verify telemetry
//        verify(telemetryService).trackEvent(
//            "ComponentTerminated", mapOf(
//                "type" to "LicenceCondition",
//                "id" to TestData.TERMINATION_LICENCE_CONDITION.id.toString(),
//                "crn" to "A000003",
//                "startDate" to "2025-01-01T12:00Z[Europe/London]",
//                "commencementDate" to null,
//                "terminationDate" to "2025-06-01T14:30+01:00[Europe/London]"
//            )
//        )
    }

    @Test
    fun `termination date updated if already terminated`() {
        val event = prepMessage("status-changed-programme-complete-requirement", wireMockServer.port())
        val terminationDate = ZonedDateTime.of(2025, 1, 1, 12, 0, 0, 0, EuropeLondon)

        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", "${TestData.TERMINATION_REQUIREMENTS[2].id}"),
                    occurredAt = terminationDate
                )
            )
        )

        val contacts =
            contactRepository.findAll().filter { it.requirement?.id == TestData.TERMINATION_REQUIREMENTS[2].id }

        // Verify the requirement termination date was updated
        val requirement = assertNotNull(requirementRepository.findByIdOrNull(TestData.TERMINATION_REQUIREMENTS[2].id))
        assertThat(requirement.terminationDate).isEqualTo(terminationDate)

        // Verify termination contact was updated
        val terminationContact =
            assertNotNull(contacts.firstOrNull { it.type.code == ContactType.COMPONENT_TERMINATED })
        assertThat(terminationContact.date).isEqualTo(terminationDate.toLocalDate())

        // Verify telemetry
//        verify(telemetryService).trackEvent(
//            "ComponentTerminationUpdated",
//            mapOf(
//                "reason" to "Programme completion occurred earlier than the start date",
//                "occurredAt" to "2024-01-01T14:30Z[Europe/London]",
//                "type" to "Requirement",
//                "id" to TestData.TERMINATION_REQUIREMENTS[1].id.toString(),
//                "crn" to "A000003",
//                "startDate" to "2030-01-01T12:00Z[Europe/London]",
//                "commencementDate" to null,
//                "terminationDate" to null
//            )
//        )
    }

    @Test
    fun `termination rejected when termination date before start date`() {
        val event = prepMessage("status-changed-programme-complete-requirement", wireMockServer.port())

        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", "${TestData.TERMINATION_REQUIREMENTS[2].id}"),
                    occurredAt = ZonedDateTime.of(2024, 1, 1, 14, 30, 0, 0, EuropeLondon)
                )
            )
        )

//        verify(telemetryService).trackEvent(
//            "ComponentTerminationRejected",
//            mapOf(
//                "reason" to "Programme completion occurred earlier than the start date",
//                "occurredAt" to "2024-01-01T14:30Z[Europe/London]",
//                "type" to "Requirement",
//                "id" to TestData.TERMINATION_REQUIREMENTS[1].id.toString(),
//                "crn" to "A000003",
//                "startDate" to "2030-01-01T12:00Z[Europe/London]",
//                "commencementDate" to null,
//                "terminationDate" to null
//            )
//        )
    }

    @Test
    fun `pending requirement transfer rejected and diary entry created on termination`() {
        val event = prepMessage("status-changed-programme-complete-requirement", wireMockServer.port())
        val terminationDate = event.message.occurredAt

        channelManager.getChannel(queueName).publishAndWait(
            event.copy(
                message = event.message.copy(
                    detailUrl = event.message.detailUrl?.replace(":id", "${TestData.TERMINATION_REQUIREMENTS[3].id}")
                )
            )
        )

        // Verify requirement was terminated
        val requirement = assertNotNull(requirementRepository.findByIdOrNull(TestData.TERMINATION_REQUIREMENTS[3].id))
        assertThat(requirement.terminationDate).isEqualTo(terminationDate)
        assertThat(requirement.pendingTransfer).isFalse()

        // Verify transfer was rejected
        val transfer =
            assertNotNull(requirementTransferRepository.findByIdOrNull(TestData.REQUIREMENT_TRANSFER.id))
        assertThat(transfer.status.code).isEqualTo(ReferenceData.REJECTED_STATUS.code)
        assertThat(transfer.decision?.code).isEqualTo(ReferenceData.REJECTED_DECISION.code)
        assertThat(transfer.rejectionReason?.code).isEqualTo(ReferenceData.REQUIREMENT_TRANSFER_REJECTION_REASON.code)
        assertThat(transfer.statusDate).isEqualTo(terminationDate)
        assertThat(transfer.notes).contains("Transfer automatically rejected due to termination of requirement")

        // Verify rejected transfer diary entry was created
        val rejectedTransferDiary = assertNotNull(
            rejectedTransferDiaryRepository.findAll()
                .singleOrNull { it.requirementTransferId == TestData.REQUIREMENT_TRANSFER.id })
        assertThat(rejectedTransferDiary.requirementId).isEqualTo(TestData.TERMINATION_REQUIREMENTS[3].id)
        assertThat(rejectedTransferDiary.personId).isEqualTo(TestData.TERMINATION_PERSON.id)
        assertThat(rejectedTransferDiary.eventId).isEqualTo(TestData.TERMINATION_COMMUNITY_EVENT.id)
        assertThat(rejectedTransferDiary.originTeamId).isEqualTo(TestData.TEAM.id)
        assertThat(rejectedTransferDiary.originStaffId).isEqualTo(TestData.STAFF.id)

        // Verify transfer rejection contact was created
        val transferContact = assertNotNull(
            contactRepository.findAll()
                .singleOrNull { it.requirement?.id == TestData.TERMINATION_REQUIREMENTS[3].id && it.type.code == ContactType.COMPONENT_TRANSFER_REJECTED })
        assertThat(transferContact.date).isEqualTo(terminationDate.toLocalDate())
        assertThat(transferContact.notes).isEqualTo(
            """
            Transfer Status: Description of TRANSFER STATUS
            Transfer Reason: null
            Rejection Reason: Description of REQUIREMENT AREA TRANSFER REJECTION REASON
            Owning Provider: Test Provider
            Receiving Provider: Test Provider
            Notes: 
            Transfer automatically rejected due to termination of requirement.
            
            """.trimIndent()
        )
    }
}