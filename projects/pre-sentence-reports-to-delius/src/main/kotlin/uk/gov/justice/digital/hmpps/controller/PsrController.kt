package uk.gov.justice.digital.hmpps.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportService
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PsrReference
import java.net.URI

@RestController
@RequestMapping("probation-cases/{crn}")
class PsrController(
    @Value("\${integrations.pre-sentence-reports.base-url}") private val psrBaseUrl: String,
    private val psrService: PreSentenceReportService
) {

    @PostMapping("/court-reports/{courtReportId}/pre-sentence-reports")
    fun createPreSentenceReport(
        @PathVariable crn: String,
        @PathVariable courtReportId: Long,
        @RequestBody createPsr: CreatePsr,
        authentication: Authentication
    ): ResponseEntity<PsrReference> {
        val report = psrService.createPreSentenceReport(
            crn = crn,
            courtReportId = courtReportId,
            reportType = createPsr.type,
            authentication.name
        )
        val url = "$psrBaseUrl/${createPsr.type}/${report.id}"
        return ResponseEntity
            .created(URI.create(url))
            .body(report)
    }
}

data class CreatePsr(val type: String)
