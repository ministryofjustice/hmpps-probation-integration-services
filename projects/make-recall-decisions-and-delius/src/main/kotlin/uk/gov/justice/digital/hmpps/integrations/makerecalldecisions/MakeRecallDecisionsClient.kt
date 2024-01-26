package uk.gov.justice.digital.hmpps.integrations.makerecalldecisions

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface MakeRecallDecisionsClient {
    @GetExchange
    fun getDetails(url: URI): RecommendationDetails

    data class RecommendationDetails(val notes: String, val sensitive: Boolean)
}
