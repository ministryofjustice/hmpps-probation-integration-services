package uk.gov.justice.digital.hmpps.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.CaseType
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.UserGenerator.AUDIT_USER
import uk.gov.justice.digital.hmpps.integrations.delius.person.CaseTypeByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.person.MostRecentInitialAllocation
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ActiveCaseServiceTest {
    @Mock
    lateinit var personRepository: PersonRepository

    lateinit var activeCaseService: ActiveCaseService

    @BeforeEach
    fun setUp() {
        activeCaseService = ActiveCaseService(personRepository, AUDIT_USER.username)
    }

    @Test
    fun `get cases is mapped and returned`() {
        val person = PersonGenerator.DEFAULT
        whenever(personRepository.findMostRecentInitialAllocations(setOf(person.crn), AUDIT_USER.username)).thenReturn(
            listOf(initialAllocation(person.crn, LocalDateTime.of(2022, 6, 24, 0, 0)))
        )
        whenever(personRepository.findCaseTypes(setOf(person.crn))).thenReturn(
            listOf(caseTypeByCrn(person.crn, CaseType.CUSTODY.name))
        )
        whenever(personRepository.findAllByCrnAndSoftDeletedFalse(listOf(person.crn))).thenReturn(listOf(person))

        val response = activeCaseService.getCases(listOf(person.crn))

        assertThat(response).hasSize(1)
        assertThat(response[0].crn).isEqualTo(person.crn)
        assertThat(response[0].name.forename).isEqualTo(person.forename)
        assertThat(response[0].type).isEqualTo(CaseType.CUSTODY.name)
        assertThat(response[0].initialAllocationDate).isEqualTo(LocalDate.of(2022, 6, 24))
    }

    @Test
    fun `get cases by group batches repository queries across the combined crn set`() {
        val crns = (1..51).map { "CRN${it.toString().padStart(4, '0')}" }
        val firstChunk = crns.take(50).toSet()
        val secondChunk = crns.drop(50).toSet()

        whenever(personRepository.findMostRecentInitialAllocations(crns.toSet(), AUDIT_USER.username)).thenReturn(emptyList())
        whenever(personRepository.findCaseTypes(firstChunk)).thenReturn(emptyList())
        whenever(personRepository.findCaseTypes(secondChunk)).thenReturn(emptyList())
        whenever(personRepository.findAllByCrnAndSoftDeletedFalse(crns)).thenReturn(emptyList())

        val response = activeCaseService.getCasesByGroup(
            mapOf(
                "A" to crns.take(30),
                "B" to crns.drop(30)
            )
        )

        assertThat(response["A"]).isEmpty()
        assertThat(response["B"]).isEmpty()
        verify(personRepository).findMostRecentInitialAllocations(crns.toSet(), AUDIT_USER.username)
        verify(personRepository).findCaseTypes(firstChunk)
        verify(personRepository).findCaseTypes(secondChunk)
        verify(personRepository).findAllByCrnAndSoftDeletedFalse(crns)
    }

    @Test
    fun `get cases by group with no crns returns empty groups and skips repository lookups`() {
        val response = activeCaseService.getCasesByGroup(
            mapOf(
                "A" to emptyList(),
                "B" to emptyList()
            )
        )

        assertThat(response).isEqualTo(mapOf("A" to emptyList<uk.gov.justice.digital.hmpps.api.model.Case>(), "B" to emptyList()))
        verifyNoInteractions(personRepository)
    }

    private fun caseTypeByCrn(caseCrn: String, caseType: String) = object : CaseTypeByCrn {
        override val crn: String = caseCrn
        override val type: String = caseType
    }

    private fun initialAllocation(crnValue: String, allocatedAtValue: LocalDateTime?) = object : MostRecentInitialAllocation {
        override val crn: String = crnValue
        override val allocatedAt: LocalDateTime? = allocatedAtValue
    }
}

