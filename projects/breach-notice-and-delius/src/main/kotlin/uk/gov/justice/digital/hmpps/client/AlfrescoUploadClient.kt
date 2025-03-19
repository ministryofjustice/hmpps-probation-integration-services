package uk.gov.justice.digital.hmpps.client

import org.springframework.http.HttpEntity
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

interface AlfrescoUploadClient {
    @PostExchange(value = "/uploadandrelease/{id}")
    fun update(@PathVariable id: String, @RequestBody body: MultiValueMap<String, HttpEntity<*>>)

    @PutExchange(value = "/lock/{id}")
    fun lock(@PathVariable id: String)

    @PutExchange(value = "/release/{id}")
    fun release(@PathVariable id: String)

    @DeleteExchange(value = "/deletehard/{id}")
    fun delete(@PathVariable id: String)
}
