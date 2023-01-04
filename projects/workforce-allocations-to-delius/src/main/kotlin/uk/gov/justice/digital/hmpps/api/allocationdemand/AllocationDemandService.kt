package uk.gov.justice.digital.hmpps.api.allocationdemand

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.AllocationDemandRequest
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.AllocationDemandResponse
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.ChoosePractitionerResponse
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.EventNumber
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.Manager
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.Name
import uk.gov.justice.digital.hmpps.api.allocationdemand.model.ProbationStatus
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManager
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository

@Service
class AllocationDemandService(
    private val allocationDemandRepository: AllocationDemandRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val staffRepository: StaffRepository,
) {
    fun findAllocationDemand(allocationDemandRequest: AllocationDemandRequest): AllocationDemandResponse {
        return AllocationDemandResponse(
            allocationDemandRepository.findAllocationDemand(
                allocationDemandRequest.cases.map {
                    Pair(
                        it.crn,
                        it.eventNumber
                    )
                }
            )
        )
    }

    fun getChoosePractitionerResponse(
        crn: String,
        eventNumber: String,
        teamCodes: List<String>
    ): ChoosePractitionerResponse {
        val person = personRepository.findByCrnAndSoftDeletedFalse(crn) ?: throw NotFoundException("Person", "crn", crn)
        val personManager = personManagerRepository.findActiveManager(person.id)
        val staffInTeams = teamCodes.associateWith { teamCode ->
            staffRepository.findAllByTeamsCode(teamCode).map { it.toManager(teamCode) }
        }
        return ChoosePractitionerResponse(
            crn = crn,
            name = person.name(),
            event = EventNumber(eventNumber),
            probationStatus = ProbationStatus(personRepository.getProbationStatus(person.crn)),
            communityPersonManager = personManager?.toManager(),
            teams = staffInTeams + mapOf("all" to staffInTeams.values.flatten())
        )
    }

    fun Person.name() = Name(forename, listOfNotNull(secondName, thirdName).joinToString(" "), surname)
    fun Staff.name() = Name(forename, middleName, surname)
    fun Staff.grade() = grade?.code?.let { GRADE_MAP[it] }
    fun Staff.toManager(teamCode: String) = Manager(code, name(), teamCode, grade())
    fun PersonManager.toManager() = staff.toManager(team.code)

    companion object {
        private val GRADE_MAP: Map<String, String> = mapOf(
            "PSQ" to "PSO",
            "PSP" to "PQiP",
            "PSM" to "PO",
            "PSC" to "SPO"
        )
    }
}
