package uk.gov.justice.digital.hmpps.integrations.crds

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface CrdsApiClient {
    @GetExchange("/sentence-and-offence-information/{bookingId}")
    fun getSentenceAndOffenceInformation(
        @PathVariable bookingId: Long
    ): List<AnalysedSentenceAndOffence>
}