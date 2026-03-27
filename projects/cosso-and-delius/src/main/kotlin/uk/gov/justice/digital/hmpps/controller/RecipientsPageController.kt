package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.EmailService

@RestController
class RecipientsPageController(private val emailService: EmailService) {
    @PreAuthorize("hasRole('PROBATION_API__COSSO__CASE_DETAILS')")
    @GetMapping("/authorised-emails")
    fun getAuthorisedEmails() = emailService.getAuthorisedEmails()
}