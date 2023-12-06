package uk.gov.justice.digital.hmpps.integrations.oasys

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.service.annotation.GetExchange

interface OrdsClient {
    @GetExchange(value = "/example/{inputId}")
    fun getExampleAPICall(@PathVariable("inputId") inputId: String): String
}
