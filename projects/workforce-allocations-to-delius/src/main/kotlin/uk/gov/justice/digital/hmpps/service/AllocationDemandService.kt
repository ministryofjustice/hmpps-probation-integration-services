package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandRequest
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandResponse
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandStaffResponse
import uk.gov.justice.digital.hmpps.api.model.AllocationDetailRequests
import uk.gov.justice.digital.hmpps.api.model.AllocationDetails
import uk.gov.justice.digital.hmpps.api.model.AllocationImpact
import uk.gov.justice.digital.hmpps.api.model.ChoosePractitionerResponse
import uk.gov.justice.digital.hmpps.api.model.Event
import uk.gov.justice.digital.hmpps.api.model.InitialAppointment
import uk.gov.justice.digital.hmpps.api.model.PrEvent
import uk.gov.justice.digital.hmpps.api.model.PrOffence
import uk.gov.justice.digital.hmpps.api.model.PrSentence
import uk.gov.justice.digital.hmpps.api.model.ProbationRecord
import uk.gov.justice.digital.hmpps.api.model.ProbationStatus
import uk.gov.justice.digital.hmpps.api.model.Requirement
import uk.gov.justice.digital.hmpps.api.model.UnallocatedEventsResponse
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.toManager
import uk.gov.justice.digital.hmpps.api.model.toStaffMember
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewRequirement
import uk.gov.justice.digital.hmpps.integrations.delius.caseview.CaseViewRequirementRepository
import uk.gov.justice.digital.hmpps.integrations.delius.contact.ContactRepository
import uk.gov.justice.digital.hmpps.integrations.delius.courtappearance.CourtAppearanceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.EventRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.AdditionalOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.AdditionalOffenceRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.DisposalRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.MainOffence
import uk.gov.justice.digital.hmpps.integrations.delius.event.sentence.SentenceWithManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrnAndSoftDeletedFalse
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.getWithUserByCode

@Service
class AllocationDemandService(
    private val allocationDemandRepository: AllocationDemandRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val staffRepository: StaffRepository,
    private val ldapService: LdapService,
    private val disposalRepository: DisposalRepository,
    private val additionalOffenceRepository: AdditionalOffenceRepository,
    private val allocationRiskService: AllocationRiskService,
    private val caseViewRequirementRepository: CaseViewRequirementRepository,
    private val eventRepository: EventRepository,
    private val contactRepository: ContactRepository,
    private val courtAppearanceRepository: CourtAppearanceRepository,
) {
    fun findAllocationDemand(allocationDemandRequest: AllocationDemandRequest): AllocationDemandResponse {
        return AllocationDemandResponse(
            allocationDemandRepository.findAllocationDemand(
                allocationDemandRequest.cases.map {
                    Pair(
                        it.crn,
                        it.eventNumber,
                    )
                },
            ),
        )
    }

    fun getChoosePractitionerResponse(
        crn: String,
        teamCodes: List<String>,
    ): ChoosePractitionerResponse {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val personManager = personManagerRepository.findActiveManager(person.id)
        val staffInTeams =
            teamCodes.associateWith { teamCode ->
                val staff = staffRepository.findActiveStaffInTeam(teamCode)
                val emails = ldapService.findEmailsForStaffIn(staff)
                staff.map { it.toStaffMember(emails[it.user?.username]) }
            }
        return ChoosePractitionerResponse(
            crn = crn,
            name = person.name(),
            probationStatus = ProbationStatus(personRepository.getProbationStatus(person.crn)),
            communityPersonManager = personManager?.toManager(),
            teams = staffInTeams,
        )
    }

    fun getProbationRecord(
        crn: String,
        eventNumber: String,
    ): ProbationRecord {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val sentences: Map<Boolean, List<SentenceWithManager>> =
            disposalRepository.findAllSentencesExcludingEventNumber(person.id, eventNumber)
                .groupBy { it.disposal.active && it.disposal.event.active }
        val additionalOffences =
            if (sentences.isNotEmpty()) {
                additionalOffenceRepository.findAllByEventIdInAndSoftDeletedFalse(
                    sentences.values.flatMap { s -> s.map { it.disposal.event.id } },
                ).groupBy { it.event.id }
            } else {
                mapOf()
            }

        return ProbationRecord(
            person.crn,
            person.name(),
            Event(eventNumber),
            sentences[true].toPrEvent(additionalOffences),
            sentences[false].toPrEvent(additionalOffences),
        )
    }

    private fun List<SentenceWithManager>?.toPrEvent(aos: Map<Long, List<AdditionalOffence>>): List<PrEvent> {
        if (isNullOrEmpty()) return listOf()
        return map {
            PrEvent(
                PrSentence(
                    it.disposal.type.description,
                    it.disposal.length,
                    it.disposal.date.toLocalDate(),
                    it.disposal.terminationDate?.toLocalDate(),
                ),
                listOf(it.mainOffence.toOffence()) + aos[it.disposal.event.id].toOffences(),
                if (it.manager.code.endsWith("U")) null else it.manager.toStaffMember(),
            )
        }
    }

    private fun MainOffence.toOffence(): PrOffence = PrOffence(offence.description, true)

    private fun List<AdditionalOffence>?.toOffences(): List<PrOffence> {
        if (isNullOrEmpty()) return listOf()
        return map { PrOffence(it.offence.description) }
    }

    fun getImpact(
        crn: String,
        staffCode: String,
    ): AllocationImpact {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val staff = staffRepository.getWithUserByCode(staffCode)
        return AllocationImpact(person.crn, person.name(), staff.toStaffMember(ldapService.findEmailForStaff(staff)))
    }

    fun getDetails(requests: AllocationDetailRequests): AllocationDetails {
        val cases =
            personRepository.findAllByCrnAndSoftDeletedFalse(requests.cases.map { it.crn })
                .associateBy { it.crn }
        val staff =
            staffRepository.findAllByCodeIn(requests.cases.map { it.staffCode })
                .map { it.toStaffMember() }
                .associateBy { it.code }
        return AllocationDetails(
            requests.cases.map {
                AllocationImpact(
                    cases[it.crn]?.crn,
                    cases[it.crn]?.name(),
                    staff[it.staffCode],
                )
            },
        )
    }

    fun getUnallocatedEvents(crn: String): UnallocatedEventsResponse {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val events = disposalRepository.findAllUnallocatedActiveEvents(person.id)
        return UnallocatedEventsResponse(person.crn, person.name(), events)
    }

    fun getAllocationDemandStaff(
        crn: String,
        eventNumber: String,
        staffCode: String,
        allocatingStaffUsername: String,
    ): AllocationDemandStaffResponse {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val staff = staffRepository.findStaffWithUserByCode(staffCode)!!
        val allocatingStaff = staffRepository.findStaffWithUserByUsername(allocatingStaffUsername)!!
        val eventId = eventRepository.findByPersonCrnAndNumber(crn, eventNumber)!!.id
        val requirements =
            caseViewRequirementRepository.findAllByDisposalEventId(eventId)
                .filter { it.mainCategory.code !in listOf("W", "W2") }
                .map { it.toRequirement() }
        val initialAppointment = contactRepository.getInitialAppointmentData(person.id, eventId)
        val emails = ldapService.findEmailsForStaffIn(listOfNotNull(staff, allocatingStaff, initialAppointment?.staff))
        return AllocationDemandStaffResponse(
            person.crn,
            person.name(),
            staff.toStaffMember(staff.user?.username?.let { emails[it] }),
            allocatingStaff.toStaffMember(allocatingStaff.user?.username?.let { emails[it] }),
            initialAppointment?.let { ia ->
                InitialAppointment(
                    ia.date,
                    ia.staff.toStaffMember(ia.staff.user?.username?.let { emails[it] }),
                )
            },
            allocationRiskService.getRiskOgrs(person),
            disposalRepository.findSentenceForEventNumberAndPersonId(person.id, eventNumber),
            courtAppearanceRepository.findOriginalCourt(eventId),
            eventRepository.findAllOffencesByEventId(eventId).sortedByDescending { it.mainOffence },
            requirements,
        )
    }

    private fun CaseViewRequirement.toRequirement() =
        Requirement(
            mainCategory.description,
            subCategory?.description,
            length?.let { "$length ${mainCategory.units?.description ?: ""}" } ?: "",
            id,
        )
}
