package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.messaging.Notifier

@RestController
@RequestMapping("update-custody-dates")
class KeyDateController(
    private val notifier: Notifier
) {
    @PostMapping
    @PreAuthorize("hasRole('PROBATION_API__CUSTODY_DATES__RW')")
    fun updateKeyDates(
        @RequestBody
        @Size(min = 1, max = 500, message = "Please provide between 0 and 500 noms numbers")
        nomsNumbers: List<String>?,
        @RequestParam(required = false, defaultValue = "true") dryRun: Boolean
    ) {
        notifier.requestBulkUpdate(nomsNumbers ?: listOf(), dryRun)
    }
}
