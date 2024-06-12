package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotActiveException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationValidator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonCode
import uk.gov.justice.digital.hmpps.integrations.delius.event.TransferReasonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamStaffContainer
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.RequirementAllocation
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import java.util.*

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

    @Mock
    private lateinit var optimisationTables: OptimisationTables

    @InjectMocks
    private lateinit var allocateRequirementService: AllocateRequirementService

    private val allocationDetail = ResourceLoader.file<RequirementAllocation>("get-requirement-allocation-body")

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

        val exception = assertThrows<IgnorableMessageException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }

        assert(exception.message.contains("Requirement not found or soft deleted in delius"))
    }

    @Test
    fun `when disposal event number not matching allocation detail event number`() {
        whenever(requirementRepository.findById(allocationDetail.requirementId))
            .thenReturn(Optional.of(RequirementGenerator.DEFAULT))

        assertThrows<ConflictException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail.copy(eventNumber = 3)
            )
        }
    }

    @Test
    fun `when disposal not active`() {
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                RequirementGenerator.generate(
                    disposal = DisposalGenerator.generate(
                        EventGenerator.generate(),
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
                    disposal = DisposalGenerator.generate(
                        EventGenerator.generate(active = false)
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
                RequirementGenerator.generate(
                    active = false,
                    disposal = DisposalGenerator.generate(
                        EventGenerator.generate()
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
                    disposal = DisposalGenerator.generate(
                        EventGenerator.generate()
                    )
                )
            )
        )

        whenever(
            requirementManagerRepository.findActiveManagerAtDate(
                allocationDetail.requirementId,
                allocationDetail.createdDate
            )
        ).thenReturn(null)

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
                    disposal = DisposalGenerator.generate(EventGenerator.generate())
                )
            )
        )

        whenever(
            requirementManagerRepository.findActiveManagerAtDate(
                allocationDetail.requirementId,
                allocationDetail.createdDate
            )
        ).thenReturn(RequirementManagerGenerator.DEFAULT)

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
            disposal = DisposalGenerator.generate(
                EventGenerator.generate()
            )
        )
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                requirement
            )
        )

        whenever(
            requirementManagerRepository.findActiveManagerAtDate(
                allocationDetail.requirementId,
                allocationDetail.createdDate
            )
        ).thenReturn(RequirementManagerGenerator.DEFAULT)

        whenever(requirementRepository.countPendingTransfers(requirement.id)).thenReturn(1)

        assertThrows<IgnorableMessageException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when  transfer reason not found`() {
        val requirement = RequirementGenerator.generate(
            disposal = DisposalGenerator.generate(
                EventGenerator.generate()
            )
        )
        whenever(requirementRepository.findById(allocationDetail.requirementId)).thenReturn(
            Optional.of(
                requirement
            )
        )

        whenever(
            requirementManagerRepository.findActiveManagerAtDate(
                allocationDetail.requirementId,
                allocationDetail.createdDate
            )
        ).thenReturn(RequirementManagerGenerator.DEFAULT)

        whenever(requirementRepository.countPendingTransfers(requirement.id)).thenReturn(0)

        whenever(transferReasonRepository.findByCode(TransferReasonCode.COMPONENT.value)).thenReturn(null)

        assertThrows<NotFoundException> {
            allocateRequirementService.createRequirementAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @ParameterizedTest(name = "Requirement with categories(main={0}, additional={1}, sub={2}) should update IAPS: {3}")
    @MethodSource("iapsCases")
    fun `update IAPS`(
        mainCategory: String?,
        additionalMainCategory: String?,
        subCategory: String?,
        shouldUpdateIaps: Boolean
    ) {
        val requirement = RequirementGenerator.generate(mainCategory, additionalMainCategory, subCategory)

        whenever(requirementRepository.findById(allocationDetail.requirementId))
            .thenReturn(Optional.of(requirement))
        whenever(
            requirementManagerRepository.findActiveManagerAtDate(
                allocationDetail.requirementId,
                allocationDetail.createdDate
            )
        )
            .thenReturn(RequirementManagerGenerator.DEFAULT)
        whenever(requirementManagerRepository.save(ArgumentMatchers.any())).thenAnswer { it.arguments[0] }
        whenever(requirementRepository.countPendingTransfers(requirement.id)).thenReturn(0)
        whenever(transferReasonRepository.findByCode(TransferReasonCode.COMPONENT.value)).thenReturn(
            TransferReasonGenerator.COMPONENT
        )
        whenever(allocationValidator.initialValidations(ProviderGenerator.DEFAULT.id, allocationDetail))
            .thenReturn(
                TeamStaffContainer(
                    TeamGenerator.DEFAULT,
                    StaffGenerator.DEFAULT,
                    ReferenceDataGenerator.INITIAL_OM_ALLOCATION
                )
            )
        whenever(contactTypeRepository.findByCode(ContactTypeCode.SENTENCE_COMPONENT_TRANSFER.value))
            .thenReturn(ContactTypeGenerator.SENTENCE_COMPONENT_TRANSFER)

        allocateRequirementService.createRequirementAllocation(PersonGenerator.DEFAULT.crn, allocationDetail)

        verify(requirementRepository, times(if (shouldUpdateIaps) 1 else 0)).updateIaps(allocationDetail.requirementId)
    }

    companion object {
        @JvmStatic
        fun iapsCases() = listOf(
            Arguments.of(null, null, null, false),
            Arguments.of("7", null, "RS66", false),
            Arguments.of("7", null, null, true),
            Arguments.of("RM38", null, null, true),
            Arguments.of("RM38", null, "RS66", true),
            Arguments.of(null, "7", null, true),
            Arguments.of(null, "7", "RS66", true),
            Arguments.of(null, "RM38", null, true),
            Arguments.of(null, "RM38", "RS66", true)
        )
    }
}
