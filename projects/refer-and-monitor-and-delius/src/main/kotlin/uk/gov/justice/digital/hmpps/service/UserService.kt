package uk.gov.justice.digital.hmpps.service

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.CaseAccess
import uk.gov.justice.digital.hmpps.api.model.Name
import uk.gov.justice.digital.hmpps.api.model.UserAccess
import uk.gov.justice.digital.hmpps.api.model.UserDetail
import uk.gov.justice.digital.hmpps.integrations.delius.limitedaccess.entity.PersonAccess
import uk.gov.justice.digital.hmpps.integrations.delius.limitedaccess.entity.UserAccessRepository
import uk.gov.justice.digital.hmpps.integrations.delius.limitedaccess.entity.isExcluded
import uk.gov.justice.digital.hmpps.integrations.delius.limitedaccess.entity.isRestricted
import uk.gov.justice.digital.hmpps.integrations.ldap.entity.LdapUserDetails
import uk.gov.justice.digital.hmpps.ldap.findByUsername

@Service
class UserService(private val uar: UserAccessRepository, private val ldapTemplate: LdapTemplate) {

    fun userDetails(username: String): UserDetail? = ldapTemplate.findByUsername<LdapUserDetails>(username)?.let {
        UserDetail(it.username, Name(it.forename, it.surname), it.email)
    }

    fun userAccessFor(username: String, crns: List<String>): UserAccess {
        val limitations: Map<String, List<PersonAccess>> = uar.getAccessFor(username, crns).groupBy { it.crn }
        return UserAccess(crns.map { limitations[it].combined(it) })
    }

    private fun List<PersonAccess>?.combined(crn: String): CaseAccess {
        return if (this == null) {
            CaseAccess(crn, userExcluded = false, userRestricted = false)
        } else {
            CaseAccess(
                crn,
                any { it.isExcluded() },
                any { it.isRestricted() },
                firstOrNull { it.isExcluded() }?.exclusionMessage,
                firstOrNull { it.isRestricted() }?.restrictionMessage
            )
        }
    }
}
