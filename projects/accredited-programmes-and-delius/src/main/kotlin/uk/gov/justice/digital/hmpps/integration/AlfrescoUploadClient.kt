package uk.gov.justice.digital.hmpps.integration

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.http.HttpEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.PostExchange

interface AlfrescoUploadClient {
    @PostExchange(value = "/uploadnew")
    fun addDocument(@RequestBody body: MultiValueMap<String, HttpEntity<*>>): AlfrescoDocument

    @DeleteExchange(value = "/deletehard/{id}")
    fun deleteDocument(@PathVariable id: String)
}

data class AlfrescoDocument(
    @JsonAlias("ID")
    val id: String
)
