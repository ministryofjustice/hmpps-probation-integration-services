package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.ResourceLoader
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementGenerator
import uk.gov.justice.digital.hmpps.data.generator.RequirementManagerGenerator
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.RequirementAllocationDetail
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class AllocateRequirementServiceTest {

    @Mock
    private lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    private lateinit var requirementRepository: RequirementRepository

    @Mock
    private lateinit var requirementManagerRepository: RequirementManagerRepository

    @Mock
    private lateinit var allocationValidator: AllocationValidator

    @Mock
    private lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    private lateinit var contactRepository: ContactRepository

    @Mock
    private lateinit var transferReasonRepository: TransferReasonRepository

    @InjectMocks
    private lateinit var allocateRequirementService: AllocateRequirementService

    private val allocationDetail =
        ResourceLoader.allocationBody("get-requirement-allocation-body") as RequirementAllocationDetail

    @Test
    fun `when requirement not for person with crn exception thrown`() {
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                RequirementGenerator.generate(
                    person = PersonGenerator.generate("NX999")
                )
            )
        )

        assertThrows<ConflictException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when requirement not found exception thrown`() {
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(Optional.empty())

        assertThrows<NotFoundException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when disposal event id not matching allocation detail event id`() {
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                RequirementGenerator.DEFAULT
            )
        )

        assertThrows<ConflictException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when disposal not active`() {
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                RequirementGenerator.generate(
                    disposal = DisposalGenerator.generate
                        (
                        EventGenerator.generate(id = allocationDetail.eventId),
                        active = false
                    )
                )
            )
        )

        assertThrows<NotActiveException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when event not active`() {
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                RequirementGenerator.generate(
                    disposal = DisposalGenerator.generate
                        (
                        EventGenerator.generate(id = allocationDetail.eventId, active = false)
                    )
                )
            )
        )

        assertThrows<NotActiveException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }


    @Test
    fun `when requirement not active`() {
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                RequirementGenerator.generate(active = false,
                    disposal = DisposalGenerator.generate
                        (
                        EventGenerator.generate(id = allocationDetail.eventId)
                    )
                )
            )
        )

        assertThrows<NotActiveException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when no requirement manager found`() {
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                RequirementGenerator.generate(
                    disposal = DisposalGenerator.generate
                        (
                        EventGenerator.generate(id = allocationDetail.eventId)
                    )
                )
            )
        )

        whenever(requirementManagerRepository.findActiveManagerAtDate(
            allocationDetail.requirementId, allocationDetail.createdDate)).thenReturn(null)

        assertThrows<NotFoundException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when duplicate allocation noop`() {

        val allocationDetail = allocationDetail.copy(
            staffCode = OrderManagerGenerator.DEFAULT.staff.code,
            teamCode = OrderManagerGenerator.DEFAULT.team.code
        )
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                RequirementGenerator.generate(
                    disposal = DisposalGenerator.generate
                        (
                        EventGenerator.generate(id = allocationDetail.eventId)
                    )
                )
            )
        )

        whenever(requirementManagerRepository.findActiveManagerAtDate(allocationDetail.requirementId, allocationDetail.createdDate))
            .thenReturn(RequirementManagerGenerator.DEFAULT)

        assertDoesNotThrow {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
        verify(requirementRepository, never()).countPendingTransfers(any())
    }

    @Test
    fun `when pending transfers exist`() {
        val requirement = RequirementGenerator.generate(
            disposal = DisposalGenerator.generate
                (
                EventGenerator.generate(id = allocationDetail.eventId)
            )
        )
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                requirement
            )
        )

        whenever(requirementManagerRepository.findActiveManagerAtDate(
            allocationDetail.requirementId, allocationDetail.createdDate)).thenReturn(RequirementManagerGenerator.DEFAULT)

        whenever(requirementRepository.countPendingTransfers(requirement.id)).thenReturn(1)

        assertThrows<ConflictException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when  transfer reason not found`() {
        val requirement = RequirementGenerator.generate(
            disposal = DisposalGenerator.generate
                (
                EventGenerator.generate(id = allocationDetail.eventId)
            )
        )
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                requirement
            )
        )

        whenever(requirementManagerRepository.findActiveManagerAtDate(
            allocationDetail.requirementId, allocationDetail.createdDate)).thenReturn(RequirementManagerGenerator.DEFAULT)

        whenever(requirementRepository.countPendingTransfers(requirement.id)).thenReturn(0)

        whenever(transferReasonRepository.findByCode(TransferReasonCode.COMPONENT.value)).thenReturn(null)

        assertThrows<NotFoundException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }


}
