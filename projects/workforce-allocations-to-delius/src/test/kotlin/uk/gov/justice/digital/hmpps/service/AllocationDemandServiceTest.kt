package uk.gov.justice.digital.hmpps.service

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.api.model.ManagementStatus.CURRENTLY_MANAGED
import uk.gov.justice.digital.hmpps.data.generator.DisposalGenerator
import uk.gov.justice.digital.hmpps.data.generator.LdapUserGenerator
import uk.gov.justice.digital.hmpps.data.generator.OffenceGenerator
import uk.gov.justice.digital.hmpps.data.generator.OrderManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonGenerator
import uk.gov.justice.digital.hmpps.data.generator.PersonManagerGenerator
import uk.gov.justice.digital.hmpps.data.generator.StaffGenerator
import uk.gov.justice.digital.hmpps.data.generator.TeamGenerator
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.courtappearance.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.document.DocumentRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.requirement.RequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.AdditionalOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.SentenceWithManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository

@ExtendWith(MockitoExtension::class)
class AllocationDemandServiceTest {
    @Mock
    lateinit var allocationDemandRepository: AllocationDemandRepository

    @Mock
    lateinit var personRepository: PersonRepository

    @Mock
    lateinit var personManagerRepository: PersonManagerRepository

    @Mock
    lateinit var staffRepository: StaffRepository

    @Mock
    lateinit var ldapService: LdapService

    @Mock
    lateinit var disposalRepository: DisposalRepository

    @Mock
    lateinit var additionalOffenceRepository: AdditionalOffenceRepository

    @Mock
    lateinit var requirementRepository: RequirementRepository

    @Mock
    lateinit var documentRepository: DocumentRepository

    @Mock
    lateinit var allocationRiskService: AllocationRiskService

    @Mock
    lateinit var caseViewRequirementRepository: CaseViewRequirementRepository

    @Mock
    lateinit var eventRepository: EventRepository

    @Mock
    lateinit var contactRepository: ContactRepository

    @Mock
    lateinit var courtAppearanceRepository: CourtAppearanceRepository

    @Mock
    lateinit var requirementTypesToIgnore: List<String>

    @InjectMocks
    lateinit var allocationDemandService: AllocationDemandService

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
        val staff = StaffGenerator.STAFF_WITH_USER
        val user = LdapUserGenerator.DEFAULT
        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(personRepository.getProbationStatus(person.crn)).thenReturn(CURRENTLY_MANAGED)
        whenever(personManagerRepository.findActiveManager(eq(person.id), any())).thenReturn(manager)
        whenever(staffRepository.findActiveStaffInTeam(team.code)).thenReturn(listOf(staff))
        whenever(ldapService.findEmailsForStaffIn(listOf(staff))).thenReturn(mapOf(user.username to user.email))

        val response = allocationDemandService.getChoosePractitionerResponse(person.crn, listOf(team.code))

        assertThat(response.probationStatus.description, equalTo("Currently managed"))
        assertThat(response.communityPersonManager!!.code, equalTo(manager.staff.code))
        assertThat(response.communityPersonManager!!.grade, equalTo("PSO"))
        assertThat(response.teams.keys, equalTo(setOf(team.code)))
        with(response.teams[team.code]!!) {
            assertThat(this.map { it.code }, equalTo(listOf(staff.code)))
            assertThat(this.map { it.email }, equalTo(listOf("example@example.com")))
        }
    }

    @ParameterizedTest
    @MethodSource("sentencesWithManagers")
    fun `get probation record correctly sorts active and non-active events`(
        sentences: List<SentenceWithManager>,
        activeCount: Int,
        inactiveCount: Int
    ) {
        val person = PersonGenerator.DEFAULT
        val eventNumber = "2"
        whenever(personRepository.findByCrnAndSoftDeletedFalse(person.crn)).thenReturn(person)
        whenever(disposalRepository.findAllSentencesExcludingEventNumber(person.id, eventNumber))
            .thenReturn(sentences)
        if (sentences.isNotEmpty()) {
            whenever(additionalOffenceRepository.findAllByEventIdInAndSoftDeletedFalse(sentences.map { it.disposal.event.id }))
                .thenAnswer { args ->
                    args.arguments
                        .mapNotNull { eventId -> sentences.firstOrNull { it.disposal.event.id == eventId } }
                        .flatMap { listOf(OffenceGenerator.generateAdditionalOffence(event = it.disposal.event)) }
                }
        }

        val res = allocationDemandService.getProbationRecord(person.crn, eventNumber)

        assertNotNull(res)
        assertThat(res.crn, equalTo(person.crn))
        assertThat(res.activeEvents.size, equalTo(activeCount))
        assertThat(res.inactiveEvents.size, equalTo(inactiveCount))
    }

    companion object {
        @JvmStatic
        fun sentencesWithManagers() = listOf(
            Arguments.of(listOf<SentenceWithManager>(), 0, 0),
            Arguments.of(
                listOf(
                    SentenceWithManager(
                        DisposalGenerator.DEFAULT,
                        OffenceGenerator.generateMainOffence(),
                        OrderManagerGenerator.DEFAULT.staff
                    )
                ),
                1,
                0
            ),
            Arguments.of(
                listOf(
                    SentenceWithManager(
                        DisposalGenerator.INACTIVE,
                        OffenceGenerator.generateMainOffence(),
                        OrderManagerGenerator.DEFAULT.staff
                    )
                ),
                0,
                1
            )
        )
    }
}
