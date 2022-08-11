package uk.gov.justice.digital.hmpps.integrations.delius.allocations

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
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.ContactTypeGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProviderGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ResponsibleOfficerGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exception.ConflictException
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.TeamStaffContainer
import uk.gov.justice.digital.hmpps.integrations.workforceallocations.AllocationDetail.PersonAllocationDetail
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

    @InjectMocks
    private lateinit var allocatePersonService: AllocatePersonService

    private val allocationDetail = ResourceLoader.file<PersonAllocationDetail>("get-person-allocation-body")

    @Test
    fun `when person not found exception thrown`() {
        whenever(personRepository.findByCrn(allocationDetail.crn)).thenReturn(null)

        assertThrows<NotFoundException> { allocatePersonService.createPersonAllocation(allocationDetail) }
    }

    @Test
    fun `when person manager not found for date exception thrown`() {
        whenever(personRepository.findByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT)
        whenever(
            personManagerRepository.findActiveManagerAtDate(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(null)

        assertThrows<NotFoundException> { allocatePersonService.createPersonAllocation(allocationDetail) }
    }

    @Test
    fun `when duplicate allocation noop`() {
        whenever(personRepository.findByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT)
        val allocationDetail = allocationDetail.copy(
            staffCode = OrderManagerGenerator.DEFAULT.staff.code,
            teamCode = OrderManagerGenerator.DEFAULT.team.code
        )
        whenever(
            personManagerRepository.findActiveManagerAtDate(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(PersonManagerGenerator.DEFAULT)

        assertDoesNotThrow { allocatePersonService.createPersonAllocation(allocationDetail) }
        verify(personRepository, never()).countPendingTransfers(any())
    }

    @Test
    fun `when pending transfer for person exception thrown`() {
        whenever(personRepository.findByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT)

        whenever(
            personManagerRepository.findActiveManagerAtDate(
                PersonGenerator.DEFAULT.id,
                allocationDetail.createdDate
            )
        ).thenReturn(PersonManagerGenerator.DEFAULT)

        whenever(personRepository.countPendingTransfers(PersonGenerator.DEFAULT.id)).thenReturn(1)

        assertThrows<ConflictException> { allocatePersonService.createPersonAllocation(allocationDetail) }
    }

    @Test
    fun `when responsible officer not found exception thrown`() {
        whenever(personRepository.findByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT)

        whenever(
            personManagerRepository.findActiveManagerAtDate(
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
        whenever(personRepository.findByCrn(allocationDetail.crn)).thenReturn(PersonGenerator.DEFAULT)

        whenever(
            personManagerRepository.findActiveManagerAtDate(
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
