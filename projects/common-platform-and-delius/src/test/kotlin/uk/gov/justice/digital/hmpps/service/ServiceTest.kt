package uk.gov.justice.digital.hmpps.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import org.springframework.jdbc.core.JdbcTemplate
import uk.gov.justice.digital.hmpps.audit.service.AuditedInteractionService
import uk.gov.justice.digital.hmpps.data.generator.*
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Equality
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.entity.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.entity.repository.*
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PersonServiceTest {
    @Mock
    lateinit var auditedInteractionService: AuditedInteractionService

    @Mock
    private lateinit var personRepository: PersonRepository

    @Mock
    private lateinit var courtRepository: CourtRepository

    @Mock
    private lateinit var equalityRepository: EqualityRepository

    @Mock
    private lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    private lateinit var teamRepository: TeamRepository

    @Mock
    private lateinit var staffRepository: StaffRepository

    @Mock
    private lateinit var referenceDataRepository: ReferenceDataRepository

    @Mock
    private lateinit var jdbcTemplate: JdbcTemplate

    @InjectMocks
    private lateinit var personService: PersonService

    @Test
    fun `insert person successfully inserts person, person manager and equality records`() {
        val person = PersonGenerator.DEFAULT
        val savedPerson = PersonGenerator.generate("A000002")
        val court = CourtGenerator.UNKNOWN_COURT_N07_PROVIDER
        val unallocatedTeam = TeamGenerator.UNALLOCATED
        val unallocatedStaff = StaffGenerator.UNALLOCATED
        val initialAllocation = ReferenceDataGenerator.INITIAL_ALLOCATION

        whenever(personRepository.save(person)).thenReturn(savedPerson)
        whenever(courtRepository.findByCode(anyString())).thenReturn(court)
        whenever(referenceDataRepository.findByCodeAndDatasetCode(anyString(), any())).thenReturn(initialAllocation)
        whenever(teamRepository.findByCode(anyString())).thenReturn(unallocatedTeam)
        whenever(staffRepository.findByCode(anyString())).thenReturn(unallocatedStaff)

        personService.insertPerson(person, court.code)

        verify(personRepository).save(person)
        verify(personManagerRepository).save(any())
        verify(equalityRepository).save(any())

        // Verify person record is saved successfully
        val personCaptor = argumentCaptor<Person>()
        verify(personRepository).save(personCaptor.capture())
        assertEquals(person.forename, personCaptor.firstValue.forename)
        assertEquals(person.surname, personCaptor.firstValue.surname)
        assertEquals(person.gender, personCaptor.firstValue.gender)

        // Verify manager record is saved successfully
        val managerCaptor = argumentCaptor<PersonManager>()
        verify(personManagerRepository).save(managerCaptor.capture())
        assertEquals(savedPerson, managerCaptor.firstValue.person)
        assertEquals(unallocatedStaff, managerCaptor.firstValue.staff)
        assertEquals(unallocatedTeam, managerCaptor.firstValue.team)
        assertEquals(court.probationArea, managerCaptor.firstValue.provider)
        assertEquals(LocalDateTime.of(1900, 1, 1, 0, 0), managerCaptor.firstValue.allocationDate)

        // Verify equality is created successfully
        val equalityCaptor = argumentCaptor<Equality>()
        verify(equalityRepository).save(equalityCaptor.capture())
        assertEquals(savedPerson.id, equalityCaptor.firstValue.personId)
        assertFalse(equalityCaptor.firstValue.softDeleted)
    }
}
