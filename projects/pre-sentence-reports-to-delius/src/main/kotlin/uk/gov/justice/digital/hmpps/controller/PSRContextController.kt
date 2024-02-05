package uk.gov.justice.digital.hmpps.controller

import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.constraints.NotNull
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportContext
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportService

@RestController
@RequestMapping(value = ["context"])
class PSRContextController(
    private val service: PreSentenceReportService
) {

    @PreAuthorize("hasAnyRole('ROLE_PSR_CONTEXT','PROBATION_API__PSR__CONTEXT')")
    @Operation(
        summary = "Probation case information related to the pre-sentence report",
        description = """Creating a pre-sentence report requires details of the relevant
            probation case. This information provides context around the case and specific
            details of the person's location, offence and current circumstances. Providing
            these details as a single context API enables the pre-sentence service to
            remove the need to access the case record and key the information into the
            Pre-Sentence service manually
        """
    )
    @GetMapping(
        value = ["{reportId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getPreSentenceReportContext(
        @NotNull @PathVariable
        reportId: String
    ): PreSentenceReportContext {
        return service.getPreSentenceReportContext(reportId)
    }
}
