package uk.gov.justice.digital.hmpps.integrations.courtcase

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface CourtCaseClient {
    @GetExchange
    fun getCourtCaseNote(baseUrl: URI): CourtCaseNote?
}
