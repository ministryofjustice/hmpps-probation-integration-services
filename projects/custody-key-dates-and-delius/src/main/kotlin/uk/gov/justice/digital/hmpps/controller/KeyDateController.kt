package uk.gov.justice.digital.hmpps.controller

import jakarta.validation.constraints.Size
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.CustodyDateUpdateService
import uk.gov.justice.digital.hmpps.telemetry.TelemetryService
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("update-custody-dates")
class KeyDateController(
    private val custodyDateUpdateService: CustodyDateUpdateService,
    private val telemetryService: TelemetryService
) {
    @PostMapping
    @PreAuthorize("haRole('PROBATION_API__CUSTODY_DATES__RW')")
    fun updateKeyDates(
        @RequestBody
        @Size(min = 1, max = 1000, message = "Please provide between 1 and 1000 noms numbers")
        nomsNumbers: List<String>,
        @RequestParam(required = false, defaultValue = "true") dryRun: Boolean
    ) {
        CompletableFuture.runAsync {
            telemetryService.trackEvent("Batch update custody key dates started")
            nomsNumbers.forEach {
                try {
                    custodyDateUpdateService.updateCustodyKeyDates(it, dryRun, "API")
                } catch (ex: Exception) {
                    telemetryService.trackEvent(
                        "KeyDateUpdateFailed",
                        mapOf("nomsNumber" to it, "message" to ex.message!!)
                    )
                }
            }
            telemetryService.trackEvent("Batch update custody key dates finished")
        }
    }
}
