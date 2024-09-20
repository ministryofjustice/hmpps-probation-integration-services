package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.*
import org.mockito.quality.Strictness
import org.springframework.dao.IncorrectResultSizeDataAccessException
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactTypeRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity.PrisonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.TeamRepository
import uk.gov.justice.digital.hmpps.test.CustomMatchers.isCloseTo
import java.time.ZonedDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
internal class PrisonManagerServiceTest {
    @Mock
    lateinit var teamRepository: TeamRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    lateinit var prisonManagerRepository: PrisonManagerRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var contactTypeRepository: ContactTypeRepository

    @Mock
    lateinit var personRepository: PersonRepository

    @InjectMocks
    lateinit var prisonManagerService: PrisonManagerService


    @BeforeEach
    fun mockSaves() {
        doAnswer<PrisonManager> { it.getArgument(0) }.whenever(prisonManagerRepository).save(any())
    }

    @Test
    fun missingAllStaffTeamIsThrown() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)

        val exception = assertThrows<NotFoundException> {
            prisonManagerService.allocateToProbationArea(
                event.disposal!!,
                ProbationAreaGenerator.DEFAULT,
                ZonedDateTime.now()
            )
        }
        assertEquals("Team with code of N02ALL not found", exception.message)
    }

    @Test
    fun missingUnallocatedStaffIsThrown() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(
            TeamGenerator.DEFAULT
        )

        val exception = assertThrows<NotFoundException> {
            prisonManagerService.allocateToProbationArea(
                event.disposal!!,
                ProbationAreaGenerator.DEFAULT,
                ZonedDateTime.now()
            )
        }
        assertEquals("Staff with code of N02UATU not found", exception.message)
    }

    @Test
    fun missingAllocationReasonIsThrown() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(
            TeamGenerator.DEFAULT
        )
        whenever(
            staffRepository.findByCodeAndTeamsId(
                "N02UATU",
                TeamGenerator.DEFAULT.id
            )
        ).thenReturn(StaffGenerator.UNALLOCATED)

        val exception = assertThrows<NotFoundException> {
            prisonManagerService.allocateToProbationArea(
                event.disposal!!,
                ProbationAreaGenerator.DEFAULT,
                ZonedDateTime.now()
            )
        }
        assertEquals("POM ALLOCATION REASON with code of AUT not found", exception.message)
    }

    @Test
    fun newPrisonManagerIsCreated() {
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id)).thenReturn(
            TeamGenerator.DEFAULT
        )
        whenever(
            staffRepository.findByCodeAndTeamsId(
                "N02UATU",
                TeamGenerator.DEFAULT.id
            )
        ).thenReturn(StaffGenerator.UNALLOCATED)
        whenever(referenceDataRepository.findByCodeAndSetName("AUT", "POM ALLOCATION REASON")).thenReturn(
            ReferenceDataGenerator.generate("TEST", ReferenceDataSetGenerator.generate("POM ALLOCATION REASON"))
        )

        prisonManagerService.allocateToProbationArea(
            event.disposal!!,
            ProbationAreaGenerator.DEFAULT,
            ZonedDateTime.now()
        )

        verify(prisonManagerRepository, times(1)).save(any())
    }

    @Test
    fun oldPrisonManagerIsEndDated() {
        mockReferenceData()
        val allocationDate = ZonedDateTime.now()
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.RECALLABLE.id, allocationDate))
            .thenReturn(
                PrisonManagerGenerator.generate(
                    PersonGenerator.RECALLABLE,
                    probationArea = ProbationAreaGenerator.generate("ANO", "Another Provider")
                )
            )

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
        mockReferenceData()
        val allocationDate = ZonedDateTime.now().minusDays(1)
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)
        whenever(
            prisonManagerRepository.findActiveManagerAtDate(
                PersonGenerator.RECALLABLE.id,
                allocationDate
            )
        ).thenReturn(
            PrisonManagerGenerator.generate(
                PersonGenerator.RECALLABLE,
                startDate = ZonedDateTime.now().minusDays(2),
                endDate = ZonedDateTime.now(),
                probationArea = ProbationAreaGenerator.generate("Prev", "Previous Provider")
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
    fun historicalPrisonManagerIsInsertedWhenThereIsAGapInHistory() {
        mockReferenceData()
        val allocationDate = ZonedDateTime.now().minusDays(2)
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)

        // Given no manager at the time of allocation
        whenever(prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.RECALLABLE.id, allocationDate))
            .thenReturn(null)
        // But there is a future-dated manager
        val futureStartDate = ZonedDateTime.now().minusDays(1)
        whenever(prisonManagerRepository.findFirstManagerAfterDate(PersonGenerator.RECALLABLE.id, allocationDate))
            .thenReturn(
                listOf(
                    PrisonManagerGenerator.generate(
                        PersonGenerator.RECALLABLE,
                        startDate = futureStartDate
                    )
                )
            )

        prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, allocationDate)

        // Then the future-dated manager is not updated
        verify(prisonManagerRepository, never()).saveAndFlush(any())

        // And we insert an end-dated historical manager
        verify(prisonManagerRepository).save(
            check { newPrisonManager ->
                assertThat(newPrisonManager.date, equalTo(allocationDate))
                assertThat(newPrisonManager.endDate, equalTo(futureStartDate))
                assertFalse(newPrisonManager.active)
            }
        )
    }

    @Test
    fun processMultiplePrisonerOffenderManagerCasesForMergedCrn() {
        mockReferenceData()
        val allocationDate = ZonedDateTime.now().minusDays(2)
        val event = EventGenerator.custodialEvent(PersonGenerator.RECALLABLE, InstitutionGenerator.DEFAULT)

        whenever(prisonManagerRepository.findActiveManagerAtDate(PersonGenerator.RECALLABLE.id, allocationDate))
            .thenThrow(IncorrectResultSizeDataAccessException::class.java)

        whenever(personRepository.findByMergedFromCrn(PersonGenerator.RECALLABLE.id)).thenReturn(PersonGenerator.RECALLABLE)

        prisonManagerService.allocateToProbationArea(event.disposal!!, ProbationAreaGenerator.DEFAULT, allocationDate)


        verify(prisonManagerRepository, never()).saveAndFlush(any())
        verify(prisonManagerRepository, never()).save(any())
        verify(prisonManagerRepository, never()).findFirstManagerAfterDate(any(), any(), any())

    }

    private fun mockReferenceData() {
        whenever(teamRepository.findByCodeAndProbationAreaId("N02ALL", ProbationAreaGenerator.DEFAULT.id))
            .thenReturn(TeamGenerator.DEFAULT)
        whenever(staffRepository.findByCodeAndTeamsId("N02UATU", TeamGenerator.DEFAULT.id))
            .thenReturn(StaffGenerator.UNALLOCATED)
        whenever(referenceDataRepository.findByCodeAndSetName("AUT", "POM ALLOCATION REASON"))
            .thenReturn(
                ReferenceDataGenerator.generate(
                    "TEST",
                    ReferenceDataSetGenerator.generate("POM ALLOCATION REASON")
                )
            )
        whenever(contactTypeRepository.findByCode(ContactType.Code.PRISON_MANAGER_AUTOMATIC_TRANSFER.value))
            .thenReturn(ReferenceDataGenerator.CONTACT_TYPE[ContactType.Code.PRISON_MANAGER_AUTOMATIC_TRANSFER])
    }
}
