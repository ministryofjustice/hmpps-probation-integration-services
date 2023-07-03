package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.check
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import uk.gov.justice.digital.hmpps.data.generator.EventGenerator
import uk.gov.justice.digital.hmpps.data.generator.InstitutionGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PrisonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.ProbationAreaGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataGenerator
import uk.gov.justice.digital.hmpps.data.generator.ReferenceDataSetGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.Contact
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeCode
import uk.gov.justice.digital.hmpps.integrations.delius.contact.type.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.responsibleofficer.ResponsibleOfficer
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.responsibleofficer.ResponsibleOfficerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.TeamRepository
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class PrisonManagerServiceTest {
    @Mock lateinit var teamRepository: TeamRepository

    @Mock lateinit var staffRepository: StaffRepository

    @Mock lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock lateinit var prisonManagerRepository: PrisonManagerRepository

    @Mock lateinit var contactRepository: ContactRepository

    @Mock lateinit var contactTypeRepository: ContactTypeRepository

    @Mock lateinit var responsibleOfficerRepository: ResponsibleOfficerRepository

    @InjectMocks lateinit var prisonManagerService: PrisonManagerService

    @BeforeEach
    fun mockSaves() {
        doAnswer<PrisonManager> { it.getArgument(0) }.whenever(prisonManagerRepository).save(any())
        doAnswer<ResponsibleOfficer> { it.getArgument(0) }.whenever(responsibleOfficerRepository).save(any())
    }

    @Test
    fun missingAllStaffTeamIsThrown() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)

        val exception = assertThrows<NotFoundException> {
            prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, ZonedDateTime.now())
        }
        assertEquals("Team with code of N02ALL not found", exception.message)
    }

    @Test
    fun missingUnallocatedStaffIsThrown() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(TeamGenerator.DEFAULT)

        val exception = assertThrows<NotFoundException> {
            prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, ZonedDateTime.now())
        }
        assertEquals("Staff with code of N02UATU not found", exception.message)
    }

    @Test
    fun missingAllocationReasonIsThrown() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(TeamGenerator.DEFAULT)
        whenever(staffRepository.findByCodeAndTeamsId("N02UATU", TeamGenerator.DEFAULT.id)).thenReturn(StaffGenerator.UNALLOCATED)

        val exception = assertThrows<NotFoundException> {
            prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, ZonedDateTime.now())
        }
        assertEquals("POM ALLOCATION REASON with code of AUT not found", exception.message)
    }

    @Test
    fun newPrisonManagerIsCreated() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(TeamGenerator.DEFAULT)
        whenever(staffRepository.findByCodeAndTeamsId("N02UATU", TeamGenerator.DEFAULT.id)).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(referenceDataRepository.findByCodeAndSetName("AUT", "POM ALLOCATION REASON")).thenReturn(ReferenceDataGenerator.generate("TEST", ReferenceDataSetGenerator.generate("POM ALLOCATION REASON")))
        doAnswer<PrisonManager> { it.getArgument(0) }.whenever(prisonManagerRepository).save(any())

        prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, ZonedDateTime.now())

        verify(prisonManagerRepository, times(1)).save(any())
    }

    @Test
    fun oldPrisonManagerIsEndDated() {
        val allocationDate = ZonedDateTime.now()
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(TeamGenerator.DEFAULT)
        whenever(staffRepository.findByCodeAndTeamsId("N02UATU", TeamGenerator.DEFAULT.id)).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(referenceDataRepository.findByCodeAndSetName("AUT", "POM ALLOCATION REASON")).thenReturn(ReferenceDataGenerator.generate("TEST", ReferenceDataSetGenerator.generate("POM ALLOCATION REASON")))
        doAnswer<PrisonManager> { it.getArgument(0) }.whenever(prisonManagerRepository).save(any())
        whenever(contactTypeRepository.findByCode(ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER])
        whenever(prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.RECALLABLE.id, allocationDate))
            .thenReturn(PrisonManagerGenerator.generate(PersonGenerator.RECALLABLE))

        prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, allocationDate)

        verify(prisonManagerRepository).saveAndFlush(
            check { oldPrisonManager ->
                assertNotNull(oldPrisonManager.endDate)
                assertFalse(oldPrisonManager.active)
            }
        )
        verify(prisonManagerRepository).save(
            check { newPrisonManager ->
                assertNull(newPrisonManager.endDate)
                assertTrue(newPrisonManager.active)
            }
        )

        verify(contactRepository).save(check { assertEquals("EPOMAT", it.type.code) })
    }

    @Test
    fun historicalPrisonManagerIsInsertedWithEndDate() {
        val allocationDate = ZonedDateTime.now().minusDays(1)
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(TeamGenerator.DEFAULT)
        whenever(staffRepository.findByCodeAndTeamsId("N02UATU", TeamGenerator.DEFAULT.id)).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(referenceDataRepository.findByCodeAndSetName("AUT", "POM ALLOCATION REASON")).thenReturn(ReferenceDataGenerator.generate("TEST", ReferenceDataSetGenerator.generate("POM ALLOCATION REASON")))
        doAnswer<PrisonManager> { it.getArgument(0) }.whenever(prisonManagerRepository).save(any())
        whenever(contactTypeRepository.findByCode(ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER])
        whenever(prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.RECALLABLE.id, allocationDate)).thenReturn(
            PrisonManagerGenerator.generate(
                PersonGenerator.RECALLABLE,
                startDate = ZonedDateTime.now().minusDays(2),
                endDate = ZonedDateTime.now()
            )
        )

        prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, allocationDate)

        val oldPrisonManager = argumentCaptor<PrisonManager>()
        val newPrisonManager = argumentCaptor<PrisonManager>()
        verify(prisonManagerRepository).saveAndFlush(oldPrisonManager.capture())
        assertNotNull(oldPrisonManager.firstValue.endDate)
        assertThat(oldPrisonManager.firstValue.endDate!!, equalTo(allocationDate))
        assertFalse(oldPrisonManager.firstValue.active)
        verify(prisonManagerRepository).save(newPrisonManager.capture())
        assertNotNull(newPrisonManager.firstValue.endDate)
        assertThat(newPrisonManager.firstValue.date, equalTo(allocationDate))
        assertThat(newPrisonManager.firstValue.endDate!!, isCloseTo(ZonedDateTime.now()))
        assertFalse(oldPrisonManager.firstValue.active)
    }

    @Test
    fun newResponsibleOfficerIsCreatedForDisposalsLongerThan20Months() {
        val allocationDate = ZonedDateTime.now()
        val event = EventGenerator.custodialEvent(
            PersonGenerator.RECALLABLE,
            InstitutionGenerator.DEFAULT,
            lengthInDays = 1000
        )
        val prisonManager = PrisonManagerGenerator.generate(PersonGenerator.RECALLABLE)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(TeamGenerator.DEFAULT)
        whenever(staffRepository.findByCodeAndTeamsId("N02UATU", TeamGenerator.DEFAULT.id)).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(referenceDataRepository.findByCodeAndSetName("AUT", "POM ALLOCATION REASON")).thenReturn(ReferenceDataGenerator.generate("TEST", ReferenceDataSetGenerator.generate("POM ALLOCATION REASON")))
        whenever(contactTypeRepository.findByCode(ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER])
        whenever(contactTypeRepository.findByCode(ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE])
        whenever(prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.RECALLABLE.id, allocationDate)).thenReturn(prisonManager)

        prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, allocationDate)

        val newResponsibleOfficer = argumentCaptor<ResponsibleOfficer>()
        verify(responsibleOfficerRepository).save(newResponsibleOfficer.capture())
        assertThat(newResponsibleOfficer.firstValue.personId, equalTo(event.person.id))
        assertThat(newResponsibleOfficer.firstValue.startDate, equalTo(allocationDate))
        assertNull(newResponsibleOfficer.firstValue.endDate)
        assertNull(newResponsibleOfficer.firstValue.communityManager)
        assertNotNull(newResponsibleOfficer.firstValue.prisonManager)

        val contact = argumentCaptor<Contact>()
        verify(contactRepository, times(2)).save(contact.capture())
        assertEquals("ROC", contact.secondValue.type.code)
        assertThat(
            contact.secondValue.notes,
            matchesPattern(
                """
            New Details:
            Responsible Officer Type: Prison Offender Manager
            Responsible Officer: Unallocated \(Unallocated Team\(N02\), NPS North East\)
            Start Date: .+?
            Allocation Reason: description of TEST
                """.trimIndent()
            )
        )
    }

    @Test
    fun existingResponsibleOfficerIsUpdatedForDisposalsLongerThan20Months() {
        val allocationDate = ZonedDateTime.now()
        val event = EventGenerator.custodialEvent(
            PersonGenerator.RECALLABLE,
            InstitutionGenerator.DEFAULT,
            lengthInDays = 1000
        )
        val personManager = PersonManagerGenerator.generate(PersonGenerator.RECALLABLE, staff = StaffGenerator.DEFAULT)
        val prisonManager = PrisonManagerGenerator.generate(PersonGenerator.RECALLABLE)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(TeamGenerator.DEFAULT)
        whenever(staffRepository.findByCodeAndTeamsId("N02UATU", TeamGenerator.DEFAULT.id)).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(referenceDataRepository.findByCodeAndSetName("AUT", "POM ALLOCATION REASON")).thenReturn(ReferenceDataGenerator.generate("TEST", ReferenceDataSetGenerator.generate("POM ALLOCATION REASON")))
        doAnswer<PrisonManager> { it.getArgument(0) }.whenever(prisonManagerRepository).save(any())
        whenever(contactTypeRepository.findByCode(ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER])
        whenever(contactTypeRepository.findByCode(ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE])
        whenever(prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.RECALLABLE.id, allocationDate)).thenReturn(prisonManager)
        whenever(responsibleOfficerRepository.findActiveManagerAtDate(eq(event.person.id), any())).thenReturn(ResponsibleOfficer(personId = event.person.id, startDate = ZonedDateTime.now(), communityManager = personManager))

        prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, allocationDate)

        val oldResponsibleOfficer = argumentCaptor<ResponsibleOfficer>()
        verify(responsibleOfficerRepository).saveAndFlush(oldResponsibleOfficer.capture())
        assertThat(oldResponsibleOfficer.firstValue.personId, equalTo(event.person.id))
        assertThat(oldResponsibleOfficer.firstValue.endDate!!, equalTo(allocationDate))

        val contact = argumentCaptor<Contact>()
        verify(contactRepository, times(2)).save(contact.capture())
        assertEquals("ROC", contact.secondValue.type.code)
        assertThat(
            contact.secondValue.notes,
            matchesPattern(
                """
            New Details:
            Responsible Officer Type: Prison Offender Manager
            Responsible Officer: Unallocated \(Unallocated Team\(N02\), NPS North East\)
            Start Date: .+?
            Allocation Reason: description of TEST

            Previous Details:
            Responsible Officer Type: Offender Manager
            Responsible Officer: Bloggs, Joe \(Unallocated Team\(N02\), NPS North East\)
            Start Date: .+?
            Allocation Reason: description of TEST
                """.trimIndent()
            )
        )
    }

    @Test
    fun historicalResponsibleOfficerIsInsertedWithEndDate() {
        val allocationDate = ZonedDateTime.now().minusDays(1)
        val event = EventGenerator.custodialEvent(
            PersonGenerator.RECALLABLE,
            InstitutionGenerator.DEFAULT,
            lengthInDays = 1000
        )
        val personManager = PersonManagerGenerator.generate(PersonGenerator.RECALLABLE, staff = StaffGenerator.DEFAULT)
        val prisonManager = PrisonManagerGenerator.generate(PersonGenerator.RECALLABLE)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(TeamGenerator.DEFAULT)
        whenever(staffRepository.findByCodeAndTeamsId("N02UATU", TeamGenerator.DEFAULT.id)).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(referenceDataRepository.findByCodeAndSetName("AUT", "POM ALLOCATION REASON")).thenReturn(ReferenceDataGenerator.generate("TEST", ReferenceDataSetGenerator.generate("POM ALLOCATION REASON")))
        doAnswer<PrisonManager> { it.getArgument(0) }.whenever(prisonManagerRepository).save(any())
        whenever(contactTypeRepository.findByCode(ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.PRISON_MANAGER_AUTOMATIC_TRANSFER])
        whenever(contactTypeRepository.findByCode(ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE.code)).thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactTypeCode.RESPONSIBLE_OFFICER_CHANGE])
        whenever(prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.RECALLABLE.id, allocationDate)).thenReturn(prisonManager)
        whenever(responsibleOfficerRepository.findActiveManagerAtDate(eq(event.person.id), any())).thenReturn(
            ResponsibleOfficer(
                personId = event.person.id,
                communityManager = personManager,
                startDate = ZonedDateTime.now().minusDays(2),
                endDate = ZonedDateTime.now()
            )
        )

        prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, allocationDate)

        verify(responsibleOfficerRepository).saveAndFlush(
            check { oldResponsibleOfficer ->
                assertThat(oldResponsibleOfficer.personId, equalTo(event.person.id))
                assertThat(oldResponsibleOfficer.endDate!!, equalTo(allocationDate))
            }
        )
        verify(responsibleOfficerRepository).save(
            check { newResponsibleOfficer ->
                assertThat(newResponsibleOfficer.startDate, equalTo(allocationDate))
                assertThat(newResponsibleOfficer.endDate!!, isCloseTo(ZonedDateTime.now()))
            }
        )
    }
}
