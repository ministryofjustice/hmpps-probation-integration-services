package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.ManagementStatus.CURRENTLY_MANAGED
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository

@ExtendWith(MockitoExtension::class)
class AllocationDemandServiceTest {
    @Mock lateinit var allocationDemandRepository: AllocationDemandRepository
    @Mock lateinit var personRepository: PersonRepository
    @Mock lateinit var personManagerRepository: PersonManagerRepository
    @Mock lateinit var staffRepository: StaffRepository
    @InjectMocks lateinit var allocationDemandService: AllocationDemandService

    @Test
    fun `missing crn for choose practitioner is thrown`() {
        val exception = assertThrows<NotFoundException> {
            allocationDemandService.getChoosePractitionerResponse("ABC", listOf())
        }
        assertThat(exception.message, equalTo("Person with crn of ABC not found"))
    }

    @Test
    fun `missing community manager for choose practitioner is handled`() {
        val person = PersonGenerator.DEFAULT
        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(personRepository.getProbationStatus(person.crn)).thenReturn(CURRENTLY_MANAGED)

        val response = allocationDemandService.getChoosePractitionerResponse(person.crn, listOf())

        assertThat(response.communityPersonManager, nullValue())
    }

    @Test
    fun `choose practitioner response is mapped and returned`() {
        val person = PersonGenerator.DEFAULT
        val manager = PersonManagerGenerator.DEFAULT
        val team = TeamGenerator.DEFAULT
        val staff = StaffGenerator.DEFAULT
        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(personRepository.getProbationStatus(person.crn)).thenReturn(CURRENTLY_MANAGED)
        whenever(personManagerRepository.findActiveManager(eq(person.id), any())).thenReturn(manager)
        whenever(staffRepository.findAllByTeamsCode(team.code)).thenReturn(listOf(staff))

        val response = allocationDemandService.getChoosePractitionerResponse(person.crn, listOf(team.code))

        assertThat(response.probationStatus.description, equalTo("Currently managed"))
        assertThat(response.communityPersonManager!!.code, equalTo(manager.staff.code))
        assertThat(response.communityPersonManager!!.grade, equalTo("PSO"))
        assertThat(response.teams.keys, equalTo(setOf(team.code, "all")))
        assertThat(response.teams[team.code]!!.map { it.code }, equalTo(listOf(staff.code)))
    }
}
