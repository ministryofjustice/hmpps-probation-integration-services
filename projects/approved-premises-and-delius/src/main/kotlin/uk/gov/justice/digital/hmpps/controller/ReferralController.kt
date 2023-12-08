package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ReferralService

@RestController
class ReferralController(private val referralService: ReferralService) {
    @PreAuthorize("hasRole('ROLE_APPROVED_PREMISES_STAFF')")
    @GetMapping("probation-case/{crn}/referrals")
    fun findReferralsForCrn(
        @PathVariable crn: String,
    ) = referralService.findExistingReferrals(crn)
}
