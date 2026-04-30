package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.personalDetails.ProbationPractitioner
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManager
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.OffenderManagerRepository
import uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity.getByCrn
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername
import org.slf4j.LoggerFactory
import org.springframework.ldap.NameNotFoundException

@Service
class GetProbationPractitioner(
    private val ppRepository: OffenderManagerRepository,
    private val ldapTemplate: LdapTemplate
) {
    companion object {
        private val log = LoggerFactory.getLogger(GetProbationPractitioner::class.java)
    }

    fun forCrn(crn: String): ProbationPractitioner {
        val offenderManager = ppRepository.getByCrn(crn)
        val username = offenderManager.staff.user?.username

        val email = username?.let { u ->
            try {
                ldapTemplate.findEmailByUsername(u)
            } catch (_: NameNotFoundException) {
                null
            } catch (ex: RuntimeException) {
                log.warn("Failed to look up LDAP email for username={}", u, ex)
                null
            }
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