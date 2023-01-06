package uk.gov.justice.digital.hmpps.api.staff

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/staff")
class StaffResource(private val service: StaffService) {

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("{code}/officer-view")
    fun officerView(
        @PathVariable code: String,
    ) = service.getOfficerView(code)
}
