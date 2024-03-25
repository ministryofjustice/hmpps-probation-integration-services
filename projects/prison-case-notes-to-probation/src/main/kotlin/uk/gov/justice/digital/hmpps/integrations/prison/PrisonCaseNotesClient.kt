package uk.gov.justice.digital.hmpps.integrations.prison

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface PrisonCaseNotesClient {
    @GetExchange
    fun getCaseNote(baseUrl: URI): PrisonCaseNote?
}
