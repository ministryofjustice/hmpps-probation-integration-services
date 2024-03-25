package uk.gov.justice.digital.hmpps.integrations.oasys

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange
import uk.gov.justice.digital.hmpps.controller.Timeline
import java.net.URI

interface OrdsClient {
    @GetExchange("/ass/allasslist/pris/{nomsId}/ALLOW")
    fun getTimeline(@PathVariable nomsId: String): Timeline

    @GetExchange("/ass/{name}/ALLOW/{id}")
    fun getSection(@PathVariable id: Long, @PathVariable name: String): ObjectNode
}
