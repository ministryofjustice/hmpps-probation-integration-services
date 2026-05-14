package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.personalDetails.ProbationPractitioner
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCrn
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername

@Service
class GetProbationPractitioner(
    private val offenderManagerRepository: OffenderManagerRepository,
    private val ldapTemplate: LdapTemplate
) {
    fun forCrn(crn: String): ProbationPractitioner {
        val offenderManager = offenderManagerRepository.getByCrn(crn)
        val username = offenderManager.staff.user?.username
        val email = username?.let {
            ldapTemplate.findEmailByUsername(it)
        }
        return offenderManager.asProbationPractitioner(email)
    }
}

private fun OffenderManager.asProbationPractitioner(email: String?): ProbationPractitioner = ProbationPractitioner(
    staff.code.normalisedStaffCode(),
    ProbationPractitioner.Name(staff.forename, staff.surname),
    ProbationPractitioner.Provider(provider.code, provider.description),
    ProbationPractitioner.Team(team.code, team.description),
    staff.isUnallocated(),
    staff.user?.username,
    email,
)