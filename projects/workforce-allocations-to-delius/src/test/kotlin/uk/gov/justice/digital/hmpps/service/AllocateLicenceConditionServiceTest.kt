package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
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
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.licencecondition.LicenceConditionRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamStaffContainer
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.LicenceConditionAllocation
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class AllocateLicenceConditionServiceTest {

    @Mock
    private lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    private lateinit var licenceConditionRepository: LicenceConditionRepository

    @Mock
    private lateinit var licenceConditionManagerRepository: LicenceConditionManagerRepository

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
    private lateinit var allocateLicenceConditionService: AllocateLicenceConditionService

    private val allocationDetail = ResourceLoader.file<LicenceConditionAllocation>("get-licence-condition-allocation-body")

    @Test
    fun `when licence condition not for person with crn exception thrown`() {
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId)).thenReturn(
            Optional.of(
                LicenceConditionGenerator.generate(
                    person = PersonGenerator.generate("NX999")
                )
            )
        )

        assertThrows<ConflictException> {
            allocateLicenceConditionService.createLicenceConditionAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when licence condition not found exception thrown`() {
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId)).thenReturn(Optional.empty())

        val exception = assertThrows<IgnorableMessageException> {
            allocateLicenceConditionService.createLicenceConditionAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }

        assert(exception.message.contains("Requirement no longer exists"))
    }

    @Test
    fun `when disposal event number not matching allocation detail event number`() {
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId))
            .thenReturn(Optional.of(LicenceConditionGenerator.DEFAULT))

        assertThrows<ConflictException> {
            allocateLicenceConditionService.createLicenceConditionAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail.copy(eventNumber = 3)
            )
        }
    }

    @Test
    fun `when disposal not active`() {
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId)).thenReturn(
            Optional.of(
                LicenceConditionGenerator.generate(
                    disposal = DisposalGenerator.generate(
                        EventGenerator.generate(),
                        active = false
                    )
                )
            )
        )

        assertThrows<NotActiveException> {
            allocateLicenceConditionService.createLicenceConditionAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when event not active`() {
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId)).thenReturn(
            Optional.of(
                LicenceConditionGenerator.generate(
                    disposal = DisposalGenerator.generate(
                        EventGenerator.generate(active = false)
                    )
                )
            )
        )

        assertThrows<NotActiveException> {
            allocateLicenceConditionService.createLicenceConditionAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when licence condition not active`() {
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId)).thenReturn(
            Optional.of(
                LicenceConditionGenerator.generate(
                    active = false,
                    disposal = DisposalGenerator.generate(
                        EventGenerator.generate()
                    )
                )
            )
        )

        assertThrows<NotActiveException> {
            allocateLicenceConditionService.createLicenceConditionAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when no licence condition manager found`() {
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId)).thenReturn(
            Optional.of(
                LicenceConditionGenerator.generate(
                    disposal = DisposalGenerator.generate(
                        EventGenerator.generate()
                    )
                )
            )
        )

        whenever(
            licenceConditionManagerRepository.findActiveManagerAtDate(
                allocationDetail.licenceConditionId,
                allocationDetail.createdDate
            )
        ).thenReturn(null)

        assertThrows<NotFoundException> {
            allocateLicenceConditionService.createLicenceConditionAllocation(
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
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId)).thenReturn(
            Optional.of(
                LicenceConditionGenerator.generate(
                    disposal = DisposalGenerator.generate(EventGenerator.generate())
                )
            )
        )

        whenever(
            licenceConditionManagerRepository.findActiveManagerAtDate(
                allocationDetail.licenceConditionId,
                allocationDetail.createdDate
            )
        ).thenReturn(LicenceConditionManagerGenerator.DEFAULT)

        assertDoesNotThrow {
            allocateLicenceConditionService.createLicenceConditionAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
        verify(licenceConditionRepository, never()).countPendingTransfers(any())
    }

    @Test
    fun `when pending transfers exist`() {
        val licenceCondition = LicenceConditionGenerator.generate(
            disposal = DisposalGenerator.generate(
                EventGenerator.generate()
            )
        )
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId)).thenReturn(
            Optional.of(licenceCondition)
        )

        whenever(
            licenceConditionManagerRepository.findActiveManagerAtDate(
                allocationDetail.licenceConditionId,
                allocationDetail.createdDate
            )
        ).thenReturn(LicenceConditionManagerGenerator.DEFAULT)

        whenever(licenceConditionRepository.countPendingTransfers(licenceCondition.id)).thenReturn(1)

        assertThrows<IgnorableMessageException> {
            allocateLicenceConditionService.createLicenceConditionAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `when transfer reason not found`() {
        val licenceCondition = LicenceConditionGenerator.generate(
            disposal = DisposalGenerator.generate(
                EventGenerator.generate()
            )
        )
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId)).thenReturn(
            Optional.of(licenceCondition)
        )

        whenever(
            licenceConditionManagerRepository.findActiveManagerAtDate(
                allocationDetail.licenceConditionId,
                allocationDetail.createdDate
            )
        ).thenReturn(LicenceConditionManagerGenerator.DEFAULT)

        whenever(licenceConditionRepository.countPendingTransfers(licenceCondition.id)).thenReturn(0)

        whenever(transferReasonRepository.findByCode(TransferReasonCode.COMPONENT.value)).thenReturn(null)

        assertThrows<NotFoundException> {
            allocateLicenceConditionService.createLicenceConditionAllocation(
                PersonGenerator.DEFAULT.crn,
                allocationDetail
            )
        }
    }

    @Test
    fun `successful allocation creates new licence condition manager`() {
        val licenceCondition = LicenceConditionGenerator.generate(
            disposal = DisposalGenerator.generate(EventGenerator.generate())
        )
        whenever(licenceConditionRepository.findById(allocationDetail.licenceConditionId))
            .thenReturn(Optional.of(licenceCondition))
        whenever(
            licenceConditionManagerRepository.findActiveManagerAtDate(
                allocationDetail.licenceConditionId,
                allocationDetail.createdDate
            )
        ).thenReturn(LicenceConditionManagerGenerator.DEFAULT)
        doAnswer { it.arguments[0] }.whenever(licenceConditionManagerRepository).save(ArgumentMatchers.any())
        whenever(licenceConditionRepository.countPendingTransfers(licenceCondition.id)).thenReturn(0)
        whenever(transferReasonRepository.findByCode(TransferReasonCode.COMPONENT.value)).thenReturn(
            TransferReasonGenerator.COMPONENT
        )
        whenever(allocationValidator.initialValidations(ProviderGenerator.DEFAULT.id, allocationDetail))
            .thenReturn(
                TeamStaffContainer(
                    TeamGenerator.DEFAULT,
                    StaffGenerator.DEFAULT,
                    ReferenceDataGenerator.INITIAL_LC_ALLOCATION
                )
            )
        whenever(contactTypeRepository.findByCode(ContactTypeCode.SENTENCE_COMPONENT_TRANSFER.value))
            .thenReturn(ContactTypeGenerator.SENTENCE_COMPONENT_TRANSFER)

        allocateLicenceConditionService.createLicenceConditionAllocation(PersonGenerator.DEFAULT.crn, allocationDetail)

        verify(licenceConditionManagerRepository, atLeastOnce()).save(ArgumentMatchers.any())
    }
}

