package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.CaseType
import uk.gov.justice.digital.hmpps.data.generator.CaseloadGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.AUDIT_USER
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.caseload.CaseloadRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseTypeByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffWithTeamsRepository
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class StaffServiceTest {
    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var caseloadRepository: CaseloadRepository

    @Mock
    lateinit var staffWithTeamsRepository: StaffWithTeamsRepository

    @Mock
    lateinit var ldapService: LdapService

    @Mock
    lateinit var personRepository: PersonRepository

    lateinit var staffService: StaffService

    @BeforeEach
    fun setUp() {
        staffService =
            StaffService(
                staffRepository,
                staffWithTeamsRepository,
                ldapService,
                personRepository,
                AUDIT_USER.username,
                caseloadRepository
            )
    }

    @Test
    fun `officer view staff not found`() {
        val exception = assertThrows<NotFoundException> {
            staffService.getOfficerView("UNK")
        }
        assertThat(exception.message, equalTo("Staff with code of UNK not found"))
    }

    @Test
    fun `active cases staff not found`() {
        val exception = assertThrows<NotFoundException> {
            staffService.getActiveCases("UNK", listOf(PersonGenerator.DEFAULT.crn))
        }
        assertThat(exception.message, equalTo("Staff with code of UNK not found"))
    }

    @Test
    fun `officer view response is mapped and returned`() {
        val staff = StaffGenerator.STAFF_WITH_USER
        whenever(ldapService.findEmailForStaff(staff)).thenReturn("test@test.com")
        whenever(staffRepository.findStaffWithUserByCode(staff.code)).thenReturn(staff)
        whenever(staffRepository.getParoleReportsDueCountByStaffId(staff.id, LocalDate.now().plusWeeks(4))).thenReturn(
            1L
        )
        whenever(staffRepository.getSentencesDueCountByStaffId(staff.id, LocalDate.now().plusWeeks(4))).thenReturn(2L)
        whenever(
            staffRepository.getKeyDateCountByCodeAndStaffId(
                staff.id,
                "EXP",
                LocalDate.now().plusWeeks(4)
            )
        ).thenReturn(3L)

        val response = staffService.getOfficerView(staff.code)

        assertThat(response.code, equalTo(staff.code))
        assertThat(response.name.forename, equalTo(staff.forename))
        assertThat(response.name.middleName, equalTo(staff.middleName))
        assertThat(response.name.surname, equalTo(staff.surname))
        assertThat(response.grade, equalTo("PSO"))
        assertThat(response.email, equalTo("test@test.com"))
        assertThat(response.paroleReportsToCompleteInNext4Weeks, equalTo(1L))
        assertThat(response.casesDueToEndInNext4Weeks, equalTo(2L))
        assertThat(response.releasesWithinNext4Weeks, equalTo(3L))
    }

    @Test
    fun `active cases response is mapped and returned`() {
        val staff = StaffGenerator.STAFF_WITH_USER
        val person = PersonGenerator.DEFAULT
        whenever(ldapService.findEmailForStaff(staff)).thenReturn("test@test.com")
        whenever(staffRepository.findStaffWithUserByCode(staff.code)).thenReturn(staff)
        whenever(personRepository.findAllByCrnAndSoftDeletedFalse(listOf(person.crn))).thenReturn(listOf(person))
        whenever(personRepository.findCaseTypes(setOf(person.crn))).thenReturn(
            listOf(
                caseTypeByCrn(
                    person.crn,
                    CaseType.CUSTODY.name
                )
            )
        )

        val response = staffService.getActiveCases(staff.code, listOf(person.crn))

        assertThat(response.code, equalTo(staff.code))
        assertThat(response.name.forename, equalTo(staff.forename))
        assertThat(response.name.middleName, equalTo(staff.middleName))
        assertThat(response.name.surname, equalTo(staff.surname))
        assertThat(response.grade, equalTo("PSO"))
        assertThat(response.email, equalTo("test@test.com"))
        assertThat(response.cases.size, equalTo(1))
        assertThat(response.cases[0].crn, equalTo(person.crn))
        assertThat(response.cases[0].name.forename, equalTo(person.forename))
        assertThat(response.cases[0].name.surname, equalTo(person.surname))
        assertThat(response.cases[0].type, equalTo(CaseType.CUSTODY.name))
        verify(personRepository).findCaseTypes(setOf(person.crn))
    }

    @Test
    fun `get active cases response is mapped and returned from caseload`() {
        val staff = StaffGenerator.STAFF_WITH_USER
        val person = PersonGenerator.DEFAULT
        val caseload = CaseloadGenerator.generate(
            person = person,
            staff = StaffGenerator.generateStaff(
                code = staff.code,
                forename = staff.forename,
                surname = staff.surname,
                teams = staff.teams,
                grade = staff.grade!!,
                id = staff.id
            )
        )
        whenever(ldapService.findEmailForStaff(staff)).thenReturn("test@test.com")
        whenever(staffRepository.findStaffWithUserByCode(staff.code)).thenReturn(staff)
        whenever(caseloadRepository.findAllByStaffCode(staff.code)).thenReturn(listOf(caseload))
        whenever(personRepository.findAllByCrnAndSoftDeletedFalse(listOf(person.crn))).thenReturn(listOf(person))
        whenever(personRepository.findCaseTypes(setOf(person.crn))).thenReturn(
            listOf(
                caseTypeByCrn(
                    person.crn,
                    CaseType.CUSTODY.name
                )
            )
        )

        val response = staffService.getActiveCases(staff.code)

        assertThat(response.code, equalTo(staff.code))
        assertThat(response.name.forename, equalTo(staff.forename))
        assertThat(response.name.middleName, equalTo(staff.middleName))
        assertThat(response.name.surname, equalTo(staff.surname))
        assertThat(response.grade, equalTo("PSO"))
        assertThat(response.email, equalTo("test@test.com"))
        assertThat(response.cases.size, equalTo(1))
        assertThat(response.cases[0].crn, equalTo(person.crn))
        assertThat(response.cases[0].name.forename, equalTo(person.forename))
        assertThat(response.cases[0].name.surname, equalTo(person.surname))
        assertThat(response.cases[0].type, equalTo(CaseType.CUSTODY.name))
        verify(personRepository).findCaseTypes(setOf(person.crn))
    }

    @Test
    fun `active cases with no crns returns empty response`() {
        val staff = StaffGenerator.STAFF_WITH_USER
        whenever(ldapService.findEmailForStaff(staff)).thenReturn("test@test.com")
        whenever(staffRepository.findStaffWithUserByCode(staff.code)).thenReturn(staff)

        val response = staffService.getActiveCases(staff.code, emptyList())

        assertThat(response.code, equalTo(staff.code))
        assertThat(response.cases.size, equalTo(0))
    }

    @Test
    fun `active cases batches repository queries when crns exceed oracle in clause limit`() {
        val staff = StaffGenerator.STAFF_WITH_USER
        val crns = (1..1001).map { "CRN${it.toString().padStart(4, '0')}" }
        val firstChunk = crns.take(999)
        val secondChunk = crns.drop(999)

        whenever(ldapService.findEmailForStaff(staff)).thenReturn("test@test.com")
        whenever(staffRepository.findStaffWithUserByCode(staff.code)).thenReturn(staff)
        whenever(personRepository.findMostRecentInitialAllocations(firstChunk.toSet(), AUDIT_USER.username)).thenReturn(emptyList())
        whenever(personRepository.findMostRecentInitialAllocations(secondChunk.toSet(), AUDIT_USER.username)).thenReturn(emptyList())
        whenever(personRepository.findCaseTypes(firstChunk.toSet())).thenReturn(emptyList())
        whenever(personRepository.findCaseTypes(secondChunk.toSet())).thenReturn(emptyList())
        whenever(personRepository.findAllByCrnAndSoftDeletedFalse(firstChunk)).thenReturn(emptyList())
        whenever(personRepository.findAllByCrnAndSoftDeletedFalse(secondChunk)).thenReturn(emptyList())

        val response = staffService.getActiveCases(staff.code, crns)

        assertThat(response.code, equalTo(staff.code))
        assertThat(response.cases.size, equalTo(0))
        verify(personRepository).findMostRecentInitialAllocations(firstChunk.toSet(), AUDIT_USER.username)
        verify(personRepository).findMostRecentInitialAllocations(secondChunk.toSet(), AUDIT_USER.username)
        verify(personRepository).findCaseTypes(firstChunk.toSet())
        verify(personRepository).findCaseTypes(secondChunk.toSet())
        verify(personRepository).findAllByCrnAndSoftDeletedFalse(firstChunk)
        verify(personRepository).findAllByCrnAndSoftDeletedFalse(secondChunk)
    }

    private fun caseTypeByCrn(caseCrn: String, caseType: String) = object : CaseTypeByCrn {
        override val crn = caseCrn
        override val type = caseType
    }
}
