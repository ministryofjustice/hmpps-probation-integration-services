package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.api.model.ReferralStarted
import uk.gov.justice.digital.hmpps.service.NsiService

@RestController
@RequestMapping("probation-case/{crn}/referrals")
class ReferralResource(private val nsiService: NsiService) {
    @PreAuthorize("hasRole('RM_REFERRAL')")
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun referralStarted(@PathVariable crn: String, @RequestBody referralStarted: ReferralStarted) {
        nsiService.startNsi(crn, referralStarted)
    }
}
