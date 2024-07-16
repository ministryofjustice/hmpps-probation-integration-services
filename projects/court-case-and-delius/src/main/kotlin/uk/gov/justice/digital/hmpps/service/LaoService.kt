package uk.gov.justice.digital.hmpps.service

import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.LaoAccess
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.PersonRepository

@Service
class LaoService(
    private val laoAccess: LaoAccess,
    val personRepository: PersonRepository
) {
    fun checkLao(crn: String) {
        personRepository.findByCrn(crn)?.let {
            if (it.currentExclusion && !laoAccess.ignoreExclusions) {
                throw AccessDeniedException(it.exclusionMessage)
            }
            if (it.currentRestriction && !laoAccess.ignoreRestrictions) {
                throw AccessDeniedException(it.restrictionMessage)
            }
        }
    }
}