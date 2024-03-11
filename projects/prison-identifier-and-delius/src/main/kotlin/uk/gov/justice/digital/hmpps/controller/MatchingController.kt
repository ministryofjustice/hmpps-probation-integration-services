package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.sevice.MatchingNotifier

@RestController
@RequestMapping("person")
class MatchingController(private val matchingNotifier: MatchingNotifier) {

    @PreAuthorize("hasRole('PROBATION_API__PRISON_IDENTIFIER__UPDATE')")
    @RequestMapping(value = ["/populate-noms-number"], method = [RequestMethod.GET, RequestMethod.POST])
    fun populateNomsNumbers(
        @RequestParam(defaultValue = "true") dryRun: Boolean,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 crns") @RequestBody crns: List<String>?
    ) {
        matchingNotifier.sendForMatch(crns ?: listOf(), dryRun)
    }
}
