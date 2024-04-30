package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.ReferralDetail
import uk.gov.justice.digital.hmpps.service.ReferralService

@RestController
@RequestMapping("probation-case/{crn}/referrals")
class ReferralController(private val referralService: ReferralService) {
    @PreAuthorize("hasRole('PROBATION_API__APPROVED_PREMISES__CASE_DETAIL')")
    @GetMapping
    fun findReferralsForCrn(@PathVariable crn: String) = referralService.findExistingReferrals(crn)

    @GetMapping("/{bookingId}")
    fun findReferralDetails(@PathVariable crn: String, @PathVariable bookingId: String): ReferralDetail =
        referralService.getReferralDetails(crn, bookingId)
}
