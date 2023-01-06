package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandRequest
import uk.gov.justice.digital.hmpps.api.model.AllocationDemandResponse
import uk.gov.justice.digital.hmpps.api.model.ChoosePractitionerResponse
import uk.gov.justice.digital.hmpps.api.model.ProbationStatus
import uk.gov.justice.digital.hmpps.api.model.name
import uk.gov.justice.digital.hmpps.api.model.toManager
import uk.gov.justice.digital.hmpps.api.model.toStaffMember
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.AllocationDemandRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.PersonRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.getByCrnAndSoftDeletedFalse
import uk.gov.justice.digital.hmpps.integrations.delius.provider.StaffRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.LdapUserRepository
import uk.gov.justice.digital.hmpps.integrations.delius.user.findEmailForStaff

@Service
class AllocationDemandService(
    private val allocationDemandRepository: AllocationDemandRepository,
    private val personRepository: PersonRepository,
    private val personManagerRepository: PersonManagerRepository,
    private val staffRepository: StaffRepository,
    private val ldapUserRepository: LdapUserRepository,
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
        teamCodes: List<String>
    ): ChoosePractitionerResponse {
        val person = personRepository.getByCrnAndSoftDeletedFalse(crn)
        val personManager = personManagerRepository.findActiveManager(person.id)
        val staffInTeams = teamCodes.associateWith {
            staffRepository.findAllByTeamsCode(it)
                .map { staff -> staff.toStaffMember(email = ldapUserRepository.findEmailForStaff(staff)) }
        }
        return ChoosePractitionerResponse(
            crn = crn,
            name = person.name(),
            probationStatus = ProbationStatus(personRepository.getProbationStatus(person.crn)),
            communityPersonManager = personManager?.toManager(),
            teams = staffInTeams + mapOf("all" to staffInTeams.values.flatten())
        )
    }
}
