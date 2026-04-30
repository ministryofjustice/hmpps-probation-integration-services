package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.personalDetails.ProbationPractitioner
import uk.gov.justice.digital.hmpps.api.model.user.UserDetails
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCrn

@Service
class GetProbationPractitioner(
    private val ppRepository: OffenderManagerRepository,
    private val userService: UserService
) {
    fun forCrn(crn: String): ProbationPractitioner {
        val offenderManager = ppRepository.getByCrn(crn)
        val email = offenderManager.staff.user?.username?.let { username ->
            runCatching { userService.getUserDetails(username).email }.getOrNull()
        }
        return offenderManager.asProbationPractitioner(email)
    }
}

private fun OffenderManager.asProbationPractitioner(email: String?): ProbationPractitioner = ProbationPractitioner(
    staff.code,
    ProbationPractitioner.Name(staff.forename, staff.surname),
    email,
    ProbationPractitioner.Provider(provider.code, provider.description),
    ProbationPractitioner.Team(team.code, team.description),
    staff.isUnallocated(),
    staff.user?.username
)