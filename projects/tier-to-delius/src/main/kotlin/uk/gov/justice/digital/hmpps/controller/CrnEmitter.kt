package uk.gov.justice.digital.hmpps.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter
import uk.gov.justice.digital.hmpps.service.CrnStreamingService
import java.util.concurrent.Executors

@Controller
class CrnEmitter(
    @Value("\${crn-streaming.timeout}") private val timeout: Long,
    private val transactionManager: PlatformTransactionManager,
    private val crnStreamingService: CrnStreamingService
) {
    private val executorService = Executors.newSingleThreadExecutor()

    @GetMapping("/probation-cases")
    fun handleSse(): ResponseBodyEmitter = ResponseBodyEmitter(timeout).also { emitter ->
        executorService.execute {
            TransactionTemplate(transactionManager).execute {
                crnStreamingService.getActiveCrns().use { stream ->
                    stream.forEach { emitter.send(it + System.lineSeparator(), MediaType.TEXT_PLAIN) }
                    emitter.complete()
                }
            }
        }
    }
}
