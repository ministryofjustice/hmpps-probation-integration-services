package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController {
    @PreAuthorize("hasRole('ROLE_EXAMPLE')")
    @GetMapping(value = ["/example/{inputId}"])
    fun handle(
        @PathVariable("inputId") inputId: String
    ) {
        // TODO Not yet implemented
    }
}
