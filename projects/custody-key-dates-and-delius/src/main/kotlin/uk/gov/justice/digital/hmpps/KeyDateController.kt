package uk.gov.justice.digital.hmpps

import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateUpdateService

@RestController
@RequestMapping("update-custody-dates")
class KeyDateController(private val custodyDateUpdateService: CustodyDateUpdateService) {
    @PostMapping
    @PreAuthorize("hasRole('ROLE_PROBATION_API__CUSTODY_DATES__UPDATE')")
    fun updateKeyDates(
        @RequestBody
        @Size(min = 1, max = 1000, message = "Please provide between 1 and 1000 noms numbers")
        nomsNumbers: List<String>,
        @RequestParam(required = false, defaultValue = "true") dryRun: Boolean
    ) {
        nomsNumbers.forEach { custodyDateUpdateService.updateCustodyKeyDates(it, dryRun) }
    }
}
