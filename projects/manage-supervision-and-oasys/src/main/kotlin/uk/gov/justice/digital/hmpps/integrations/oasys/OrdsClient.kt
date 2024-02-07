package uk.gov.justice.digital.hmpps.integrations.oasys

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface OrdsClient {
    @GetExchange("/ass/allasslist/pris/{nomsId}/ALLOW")
    fun getTimeline(@PathVariable nomsId: String): Timeline
}
