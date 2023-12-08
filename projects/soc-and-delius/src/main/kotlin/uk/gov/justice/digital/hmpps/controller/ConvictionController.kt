package uk.gov.justice.digital.hmpps.controller

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.service.ConvictionService

@RestController
class ConvictionController(private val convictionService: ConvictionService) {
    @PreAuthorize("hasRole('ROLE_SOC_PROBATION_CASE')")
    @GetMapping(value = ["/convictions/{value}"])
    fun convictions(
        @PathVariable value: String,
        @RequestParam(required = false, defaultValue = "CRN") type: IdentifierType,
        @RequestParam(required = false, defaultValue = "false") activeOnly: Boolean,
    ) = convictionService.getConvictions(value, type, activeOnly)
}
