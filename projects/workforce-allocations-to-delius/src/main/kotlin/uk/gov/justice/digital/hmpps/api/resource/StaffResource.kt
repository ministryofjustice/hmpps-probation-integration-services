package uk.gov.justice.digital.hmpps.api.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.StaffService

@Validated
@RestController
@RequestMapping("/staff")
class StaffResource(private val service: StaffService) {

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @GetMapping("{code}/officer-view")
    fun officerView(
        @PathVariable code: String,
    ) = service.getOfficerView(code)

    @PreAuthorize("hasRole('ROLE_ALLOCATION_CONTEXT')")
    @PostMapping("{code}/active-cases")
    fun activeCases(
        @PathVariable code: String,
        @RequestBody crns: List<String>
    ) = service.getActiveCases(code, crns)
}
