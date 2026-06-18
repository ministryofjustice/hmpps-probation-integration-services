package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.model.SignAndSendResponse
import uk.gov.justice.digital.hmpps.service.SignAndSendService

@RestController
class SignAndSendController(private val signAndSendService: SignAndSendService) {
    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    @GetMapping("/sign-and-send/{crn}/{username}")
    fun getSignAndSend(@PathVariable crn: String, @PathVariable username: String):
        SignAndSendResponse = signAndSendService.getSignAndSend(crn, username)
}