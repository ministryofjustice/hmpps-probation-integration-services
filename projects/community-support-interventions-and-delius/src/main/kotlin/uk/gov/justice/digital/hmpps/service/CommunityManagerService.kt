package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.controller.model.CommunityManager
import uk.gov.justice.digital.hmpps.controller.model.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.controller.model.Name
import uk.gov.justice.digital.hmpps.entity.CommunityManagerRepository
import uk.gov.justice.digital.hmpps.entity.StaffUserRepository
import uk.gov.justice.digital.hmpps.entity.TeamRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException.Companion.orNotFoundBy

@Service
class CommunityManagerService(
    private val ldapService: LdapService,
    private val staffUserRepository: StaffUserRepository,
    private val communityManagerRepository: CommunityManagerRepository,
    private val teamRepository: TeamRepository,
) {
    fun getCommunityManagerForCrn(crn: String): CommunityOffenderManager {
        val communityOffenderManager = communityManagerRepository.findByPersonCrn(crn).orNotFoundBy("crn", crn)
        val staffId = communityOffenderManager.staff.id
        val staffUser = staffUserRepository.findByStaffId(staffId)
        val staff = communityOffenderManager.staff
        val emailAddress = ldapService.findEmailForUsername(staffUser?.userName)
        val name = Name(staff.forename, staff.middleName, staff.surname)
        val teamCode = ldapService.findTeamForUsername(staffUser?.userName)?.toLong()
        val team = teamRepository.findById(teamCode!!).get()
        val pdu = team.district?.borough?.description
        return CommunityOffenderManager(
            crn = crn,
            communityManager = CommunityManager(
                jobRole = staff.jobRole?.description,
                emailAddress = emailAddress,
                pdu = pdu,
                officeName = team.officeLocation?.description,
                name = name,
                teamPhoneNumber = team.telephone
            )
        )
    }
}