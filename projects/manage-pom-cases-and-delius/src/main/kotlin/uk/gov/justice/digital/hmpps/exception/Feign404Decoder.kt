package uk.gov.justice.digital.hmpps.exception

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import feign.FeignException.errorStatus
import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.stereotype.Component
import java.lang.Exception

@Component
class Feign404Decoder(private val objectMapper: ObjectMapper) : ErrorDecoder {
    override fun decode(methodKey: String?, response: Response): Exception? {
        return response.body().asInputStream()?.use { ins ->
            objectMapper.readValue<ErrorResponse>(ins).message?.let {
                NotAllocatedException(
                    when (it) {
                        "Not allocated" -> NotAllocatedException.Reason.DEALLOCATED
                        else -> NotAllocatedException.Reason.PRE_ALLOCATION
                    }
                )
            } ?: errorStatus(methodKey, response)
        }
    }
}

data class ErrorResponse(val message: String?)
class NotAllocatedException(val reason: Reason) : Exception("POM Not Allocated: $reason") {
    enum class Reason {
        DEALLOCATED, PRE_ALLOCATION
    }
}
