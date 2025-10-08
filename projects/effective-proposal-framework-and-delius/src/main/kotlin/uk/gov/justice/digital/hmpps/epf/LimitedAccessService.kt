package uk.gov.justice.digital.hmpps.epf

import org.springframework.ldap.core.LdapTemplate
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.epf.entity.ExclusionRepository
import uk.gov.justice.digital.hmpps.epf.entity.Person
import uk.gov.justice.digital.hmpps.epf.entity.RestrictionRepository
import uk.gov.justice.digital.hmpps.ldap.findEmailByUsername

@Service
class LimitedAccessService(
    private val exclusionRepository: ExclusionRepository,
    private val restrictionRepository: RestrictionRepository,
    private val ldap: LdapTemplate,
) {
    fun getLimitedAccessDetails(person: Person): LimitedAccessDetail {
        val excludedFrom = exclusionRepository.findActiveExclusions(person.id).map {
            LimitedAccess.ExcludedFrom(email = ldap.findEmailByUsername(it.user.username) ?: "Unknown")
        }
        val restrictedTo = restrictionRepository.findActiveRestrictions(person.id).map {
            LimitedAccess.RestrictedTo(email = ldap.findEmailByUsername(it.user.username) ?: "Unknown")
        }
        return LimitedAccessDetail(
            excludedFrom = excludedFrom,
            exclusionMessage = person.exclusionMessage.takeIf { excludedFrom.isNotEmpty() },
            restrictedTo = restrictedTo,
            restrictionMessage = person.restrictionMessage.takeIf { excludedFrom.isNotEmpty() },
        )
    }
}