package uk.gov.justice.digital.hmpps.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PreSentenceReportService
import uk.gov.justice.digital.hmpps.integrations.delius.presentencereport.PsrReference
import java.net.URI
import java.util.UUID

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
        return ResponseEntity.created(URI.create(url)).body(report)
    }

    @GetMapping("/pre-sentence-reports")
    fun findUrl(
        @PathVariable crn: String,
        @RequestParam urn: String,
        authentication: Authentication
    ): ResponseEntity<PsrReference> {
        val uuid = try {
            UUID.fromString(urn.substring(urn.lastIndexOf(":") + 1))
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "URN $urn is invalid.")
        }
        val url = urn.apply {
            when {
                startsWith("urn:uk:gov:hmpps:pre-sentence-service:report:") ->
                    psrService.getPreSentenceReportUrl(uuid)

                startsWith("urn:uk:gov:hmpps:alfresco:document:") ->
                    psrService.getLegacyNewTechReportUrl(uuid, authentication.name)

                else -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported URN $urn")
            }
        }
        return ResponseEntity.ok().location(URI.create(url)).body(PsrReference(uuid, urn))
    }
}

data class CreatePsr(val type: String)
