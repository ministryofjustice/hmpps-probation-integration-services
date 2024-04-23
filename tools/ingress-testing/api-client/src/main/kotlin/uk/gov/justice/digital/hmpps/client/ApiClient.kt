package uk.gov.justice.digital.hmpps.client

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class ApiClient(private val restClient: RestClient) {
    fun getTest(delay: Int) = restClient
        .get()
        .uri("/test/$delay")
        .exchange { _, res -> res.statusCode.toString() }
}
