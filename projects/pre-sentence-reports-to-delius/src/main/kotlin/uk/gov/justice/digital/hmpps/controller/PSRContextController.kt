package uk.gov.justice.digital.hmpps.controller

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.NotNull

@RestController
@RequestMapping(value = ["context"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PSRContextController(
    private val service: PreSentenceReportService,
) {
    @GetMapping(
        value = ["{reportId}"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    @ResponseStatus(HttpStatus.OK)
    fun getPreSentenceReportContext(
        @NotNull @PathVariable reportId: String
    ): PreSentenceReportContext {
        return service.getPreSentenceReportContext(reportId)
    }
}
