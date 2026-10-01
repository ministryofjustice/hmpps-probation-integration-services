package uk.gov.justice.digital.hmpps.client

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.http.HttpEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

interface AlfrescoUploadClient {
    @PutExchange(value = "/release/{id}")
    fun release(@PathVariable id: String)

    @DeleteExchange(value = "/deletehard/{id}")
    fun delete(@PathVariable id: String)

    @PostExchange(value = "/uploadnew")
    fun upload(@RequestBody body: MultiValueMap<String, HttpEntity<*>>): AlfrescoDocument
}

data class AlfrescoDocument(
    @JsonAlias("ID")
    val id: String
)
