package uk.gov.justice.digital.hmpps.integrations.makerecalldecisions

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import uk.gov.justice.digital.hmpps.config.OAuth2FeignConfig
import java.net.URI

@FeignClient(name = "make-recall-decisions", url = "https://dummy-url/to/be/overridden", configuration = [OAuth2FeignConfig::class])
interface MakeRecallDecisionsClient {
    @GetMapping
    fun getDetails(url: URI): DecisionDetails

    data class DecisionDetails(val notes: String, val sensitive: Boolean)
}
