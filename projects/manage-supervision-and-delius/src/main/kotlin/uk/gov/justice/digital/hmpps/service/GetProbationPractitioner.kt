package uk.gov.justice.digital.hmpps.service


import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.personalDetails.ProbationPractitioner
import uk.gov.justice.digital.hmpps.api.model.user.UserDetails
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCrn
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User

@Service
class GetProbationPractitioner(private val ppRepository: OffenderManagerRepository, private val userService: UserService) {
    fun forCrn(crn: String): ProbationPractitioner {
        val offenderManager = ppRepository.getByCrn(crn)
        val user = userService.getUserDetails(username = offenderManager.staff.user?.username!!)
        return offenderManager.asProbationPractitioner(user)
    }
}
private fun OffenderManager.asProbationPractitioner(user: UserDetails): ProbationPractitioner = ProbationPractitioner(
    staff.code,
    ProbationPractitioner.Name(staff.forename, staff.surname),
    user.email,
    ProbationPractitioner.Provider(provider.code, provider.description),
    ProbationPractitioner.Team(team.code, team.description),
    staff.isUnallocated(),
    staff.user?.username
)