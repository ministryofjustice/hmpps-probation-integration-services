package uk.gov.justice.digital.hmpps.detail

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.message.HmppsDomainEvent
import java.net.URI

@Service
class DomainEventDetailService(
    @Qualifier("oauth2Client") val restClient: RestClient?,
    @Value("\${messaging.consumer.detail.urls:#{null}}") val allowedUrls: List<String>?
) {
    fun validate(detailUrl: String?): URI {
        val uri = URI.create(requireNotNull(detailUrl) { "Detail URL must not be null" })
        val baseUrl = "${uri.scheme}://${uri.authority}"
        require(allowedUrls == null || allowedUrls!!.contains(baseUrl)) { "Unexpected detail URL: $baseUrl" }
        return uri
    }

    final inline fun <reified T> getDetail(event: HmppsDomainEvent): T =
        getDetail(validate(event.detailUrl), object : ParameterizedTypeReference<T>() {})

    final inline fun <reified T> getDetail(detailUrl: String?): T =
        getDetail(validate(detailUrl), object : ParameterizedTypeReference<T>() {})

    fun <T> getDetail(uri: URI, type: ParameterizedTypeReference<T>): T =
        requireNotNull(restClient).get().uri(uri).retrieve().body<T>(type)!!

    final inline fun <reified T> getDetailResponse(event: HmppsDomainEvent): ResponseEntity<T> =
        getDetailResponse(validate(event.detailUrl), object : ParameterizedTypeReference<T>() {})

    fun <T> getDetailResponse(uri: URI, type: ParameterizedTypeReference<T>): ResponseEntity<T> =
        requireNotNull(restClient).get().uri(uri).retrieve().toEntity<T>(type)
}