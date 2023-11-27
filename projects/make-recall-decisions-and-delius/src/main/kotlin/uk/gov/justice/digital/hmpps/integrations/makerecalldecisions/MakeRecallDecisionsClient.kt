package uk.gov.justice.digital.hmpps.integrations.makerecalldecisions

import org.springframework.web.service.annotation.GetExchange
import java.net.URI

interface MakeRecallDecisionsClient {
    @GetExchange
    fun getDetails(url: URI): DecisionDetails

    data class DecisionDetails(val notes: String, val sensitive: Boolean)
}
