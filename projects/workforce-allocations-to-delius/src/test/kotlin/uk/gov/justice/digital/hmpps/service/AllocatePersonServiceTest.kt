package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.audit.service.OptimisationTables
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationValidator
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.*
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamStaffContainer
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.PersonAllocation
import uk.gov.justice.digital.hmpps.resourceloader.ResourceLoader

@ExtendWith(MockitoExtension::class)
internal class AllocatePersonServiceTest {

    @Mock
    private lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    private lateinit var personRepository: PersonRepository

    @Mock
    private lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    private lateinit var allocationValidator: AllocationValidator

    @Mock
    private lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    private lateinit var contactRepository: ContactRepository

    @Mock
    private lateinit var responsibleOfficerRepository: ResponsibleOfficerRepository

    @Mock
    private lateinit var optimisationTables: OptimisationTables

    @InjectMocks
    private lateinit var allocatePersonService: AllocatePersonService

    private val allocationDetail = ResourceLoader.file<PersonAllocation>("get-person-allocation-body")

    @Test
    fun `when person not found exception thrown`() {
        whenever(personRepository.findIdByCrn(allocationDetail.crn)).thenReturn(null)

        assertThrows<NotFoundException> { allocatePersonService.createPersonAllocation(allocationDetail) }
    }

    @Test
    fun `when person manager not found for date exception thrown`() {
        whenever(personRepository.findIdByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT.id)
        whenever(
            personManagerRepository.findActiveManager(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(null)

        assertThrows<NotFoundException> { allocatePersonService.createPersonAllocation(allocationDetail) }
    }

    @Test
    fun `when duplicate allocation noop`() {
        whenever(personRepository.findIdByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT.id)
        val allocationDetail = allocationDetail.copy(
            staffCode = OrderManagerGenerator.DEFAULT.staff.code,
            teamCode = OrderManagerGenerator.DEFAULT.team.code
        )
        whenever(
            personManagerRepository.findActiveManager(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(PersonManagerGenerator.DEFAULT)

        assertDoesNotThrow { allocatePersonService.createPersonAllocation(allocationDetail) }
        verify(personRepository, never()).countPendingTransfers(any())
    }

    @Test
    fun `when pending transfer for person exception thrown`() {
        whenever(personRepository.findIdByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT.id)

        whenever(
            personManagerRepository.findActiveManager(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(PersonManagerGenerator.DEFAULT)

        whenever(personRepository.countPendingTransfers(PersonGenerator.DEFAULT.id)).thenReturn(1)

        assertThrows<IgnorableMessageException> { allocatePersonService.createPersonAllocation(allocationDetail) }
    }

    @Test
    fun `when responsible officer not found exception thrown`() {
        whenever(personRepository.findIdByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT.id)

        whenever(
            personManagerRepository.findActiveManager(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(PersonManagerGenerator.DEFAULT)

        whenever(personRepository.countPendingTransfers(PersonGenerator.DEFAULT.id)).thenReturn(0)
        whenever(allocationValidator.initialValidations(ProviderGenerator.DEFAULT.id, allocationDetail))
            .thenReturn(
                TeamStaffContainer(
                    TeamGenerator.DEFAULT,
                    StaffGenerator.DEFAULT,
                    ReferenceDataGenerator.INITIAL_OM_ALLOCATION
                )
            )

        whenever(personManagerRepository.save(any<PersonManager>())).thenAnswer { it.arguments[0] }

        whenever(
            responsibleOfficerRepository.findActiveManagerAtDate(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(null)

        assertThrows<NotFoundException> { allocatePersonService.createPersonAllocation(allocationDetail) }
    }

    @Test
    fun `when responsible officer is prison manager detail noted in contact notes`() {
        whenever(personRepository.findIdByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT.id)

        whenever(
            personManagerRepository.findActiveManager(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(PersonManagerGenerator.DEFAULT)

        whenever(personRepository.countPendingTransfers(PersonGenerator.DEFAULT.id)).thenReturn(0)
        whenever(allocationValidator.initialValidations(ProviderGenerator.DEFAULT.id, allocationDetail))
            .thenReturn(
                TeamStaffContainer(
                    TeamGenerator.DEFAULT,
                    StaffGenerator.DEFAULT,
                    ReferenceDataGenerator.INITIAL_OM_ALLOCATION
                )
            )

        whenever(personManagerRepository.save(any<PersonManager>())).thenAnswer { it.arguments[0] }

        whenever(
            responsibleOfficerRepository.findActiveManagerAtDate(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(
            ResponsibleOfficerGenerator.generate(
                communityManager = null,
                prisonManager = PrisonManager(
                    23,
                    PersonGenerator.DEFAULT.id,
                    ProviderGenerator.DEFAULT,
                    TeamGenerator.DEFAULT,
                    StaffGenerator.DEFAULT
                )
            )
        )

        whenever(contactTypeRepository.findByCode(ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE.value))
            .thenReturn(ContactTypeGenerator.RESPONSIBLE_OFFICER_CHANGE)
        whenever(contactTypeRepository.findByCode(ContactTypeCode.OFFENDER_MANAGER_TRANSFER.value))
            .thenReturn(ContactTypeGenerator.OFFENDER_MANAGER_TRANSFER)

        allocatePersonService.createPersonAllocation(allocationDetail)
        verify(responsibleOfficerRepository).save(any())

        val contactCaptor = ArgumentCaptor.forClass(Contact::class.java)
        verify(contactRepository, times(2)).save(contactCaptor.capture())

        val contact = contactCaptor.allValues[0]
        assertThat(contact.notes, containsString("Responsible Officer Type: Prison Offender Manager"))
    }
}
