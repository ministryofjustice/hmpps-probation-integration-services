package uk.gov.justice.digital.hmpps.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.api.model.UserAccess
import uk.gov.justice.digital.hmpps.integrations.delius.user.access.UserAccessRepository

@Service
class UserAccessService(private val uar: UserAccessRepository) {
    fun userAccessFor(username: String, crns: List<String>): Map<String, UserAccess> {
        val limitations = uar.getAccessFor(username, crns).associate {
            Pair(
                it.crn,
                UserAccess(
                    userExcluded = it.isExcluded(),
                    userRestricted = it.isRestricted(),
                    exclusionMessage = it.exclusionMessage,
                    restrictionMessage = it.restrictionMessage
                )
            )
        }
        return crns.associateWith { limitations[it] ?: UserAccess.NO_ACCESS_LIMITATIONS }
    }
}
