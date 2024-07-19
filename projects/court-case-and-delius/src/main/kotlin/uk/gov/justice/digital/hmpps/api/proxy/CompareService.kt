package uk.gov.justice.digital.hmpps.api.proxy

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
class CompareService(
    private val objectMapper: ObjectMapper,
    private val applicationContext: ApplicationContext
) {

    fun toJsonString(compare: Compare): String {
        val uri = Uri.valueOf(compare.uri)
        val instance = applicationContext.getBean(uri.ccdInstance)
        return objectMapper.writeValueAsString(instance::class.members.firstOrNull { it.name == uri.ccdFunction }
            ?.call(instance, compare.crn))
    }
}