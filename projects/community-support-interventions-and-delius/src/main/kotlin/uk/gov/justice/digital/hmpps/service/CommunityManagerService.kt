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
    fun getCommunityManagerForCrn(crn: String) =
        with(communityManagerRepository.findByPersonCrn(crn).orNotFoundBy("crn", crn)) {
            val emailAddress = staff.user?.userName?.let { ldapService.findEmailForUsername(it) }
            CommunityOffenderManager(
                crn = crn,
                communityManager = CommunityManager(
                    name = Name(staff.forename, staff.middleName, staff.surname),
                    jobRole = staff.jobRole?.description,
                    officeName = team.officeLocation?.description,
                    pdu = team.district.borough.description,
                    emailAddress = emailAddress,
                    teamPhoneNumber = team.telephone,
                )
            )
        }
}