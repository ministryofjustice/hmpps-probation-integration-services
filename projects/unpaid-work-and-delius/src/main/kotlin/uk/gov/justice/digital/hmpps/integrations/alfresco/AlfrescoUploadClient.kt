package uk.gov.justice.digital.hmpps.integrations.alfresco

import org.springframework.http.HttpEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange

interface AlfrescoUploadClient {
    @PostExchange(value = "/uploadnew")
    fun addDocument(@RequestBody body: MultiValueMap<String, HttpEntity<*>>): AlfrescoDocument
}
