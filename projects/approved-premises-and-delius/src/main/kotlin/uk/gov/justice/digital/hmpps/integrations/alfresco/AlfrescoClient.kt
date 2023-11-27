package uk.gov.justice.digital.hmpps.integrations.alfresco

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import uk.gov.justice.digital.hmpps.security.ServiceContext

@Component
class AlfrescoClient(@Value("\${integrations.alfresco.url}") private val alfrescoBaseUrl: String) {
    private val restClient = RestClient.builder().baseUrl(alfrescoBaseUrl).build()

    fun getDocument(id: String): ResponseEntity<Resource> = restClient.get().uri("/fetch/$id")
        .accept(MediaType.MULTIPART_FORM_DATA)
        .headers {
            it["X-DocRepository-Remote-User"] = "N00"
            it["X-DocRepository-Real-Remote-User"] = ServiceContext.servicePrincipal()!!.username
            it[HttpHeaders.ACCEPT] = MediaType.MULTIPART_FORM_DATA_VALUE
        }
        .retrieve()
        .toEntity(Resource::class.java)
}
