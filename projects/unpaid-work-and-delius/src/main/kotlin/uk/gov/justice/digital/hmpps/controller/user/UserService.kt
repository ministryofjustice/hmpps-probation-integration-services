package uk.gov.justice.digital.hmpps.controller.user

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.service.UserAccessService

@Service
class UserService(private val userAccessService: UserAccessService) {

    fun userAccessFor(username: String, crn: String) = userAccessService.caseAccessFor(username, crn)
}
