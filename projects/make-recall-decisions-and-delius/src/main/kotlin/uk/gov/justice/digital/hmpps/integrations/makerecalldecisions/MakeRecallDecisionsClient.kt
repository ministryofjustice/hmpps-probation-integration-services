package uk.gov.justice.digital.hmpps.integrations.makerecalldecisions

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import java.net.URI

@FeignClient(name = "make-recall-decisions", url = "https://dummy-url/to/be/overridden")
fun interface MakeRecallDecisionsClient {
    @GetMapping
    fun getDetails(url: URI): DecisionDetails

    data class DecisionDetails(val notes: String, val sensitive: Boolean)
}
