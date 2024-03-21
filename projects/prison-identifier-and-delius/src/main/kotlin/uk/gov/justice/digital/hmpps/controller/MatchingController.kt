package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.messaging.Notifier

@RestController
@RequestMapping("person")
class MatchingController(private val notifier: Notifier) {

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('PROBATION_API__PRISON_IDENTIFIER__UPDATE')")
    @RequestMapping(value = ["/match-by-crn"], method = [RequestMethod.GET, RequestMethod.POST])
    fun matchByCrn(
        @RequestParam(defaultValue = "true") dryRun: Boolean,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 CRNs. Leave blank to match all CRNs.")
        @RequestBody crns: List<String>?
    ) {
        Thread.ofVirtual().start { notifier.requestPrisonMatching(crns ?: listOf(), dryRun) }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasRole('PROBATION_API__PRISON_IDENTIFIER__UPDATE')")
    @RequestMapping(value = ["/match-by-noms"], method = [RequestMethod.GET, RequestMethod.POST])
    fun matchByNoms(
        @RequestParam(defaultValue = "true") dryRun: Boolean,
        @Size(min = 1, max = 500, message = "Please provide between 1 and 500 NOMS numbers.")
        @RequestBody nomsNumbers: List<String>
    ) {
        Thread.ofVirtual().start { notifier.requestProbationMatching(nomsNumbers, dryRun) }
    }
}
