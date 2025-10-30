package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.personalDetails.ProbationPractitioner
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCrn

@Service
class GetProbationPractitioner(private val ppRepository: OffenderManagerRepository) {
    fun forCrn(crn: String): ProbationPractitioner = ppRepository.getByCrn(crn).asProbationPractitioner()
}

private fun OffenderManager.asProbationPractitioner(): ProbationPractitioner = ProbationPractitioner(
    staff.code,
    ProbationPractitioner.Name(staff.forename, staff.surname),
    ProbationPractitioner.Provider(provider.code, provider.description),
    ProbationPractitioner.Team(team.code, team.description),
    staff.isUnallocated(),
    staff.user?.username
)