package uk.gov.justice.digital.hmpps.controller

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportContext
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportService
import javax.validation.constraints.NotNull

@RestController
@RequestMapping(value = ["context"])
class PSRContextController(
    private val service: PreSentenceReportService,
) {

    @PreAuthorize("hasRole('ROLE_PSR_CONTEXT')")
    @GetMapping(
        value = ["{reportId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getPreSentenceReportContext(
        @NotNull @PathVariable reportId: String
    ): PreSentenceReportContext {
        return service.getPreSentenceReportContext(reportId)
    }
}
