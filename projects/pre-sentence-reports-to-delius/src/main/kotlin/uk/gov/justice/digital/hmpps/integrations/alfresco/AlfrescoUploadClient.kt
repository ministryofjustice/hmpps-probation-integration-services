package uk.gov.justice.digital.hmpps.integrations.alfresco

import org.springframework.http.HttpEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

interface AlfrescoUploadClient {
    @PutExchange(value = "/release/{id}")
    fun releaseDocument(@PathVariable id: String)

    @PostExchange(value = "/uploadandrelease/{id}")
    fun updateDocument(@PathVariable id: String, @RequestBody body: MultiValueMap<String, HttpEntity<*>>)
}
