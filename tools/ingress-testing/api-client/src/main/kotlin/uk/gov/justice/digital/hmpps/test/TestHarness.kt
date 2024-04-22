package uk.gov.justice.digital.hmpps.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication.exit
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.client.ApiClient
import java.util.concurrent.CompletableFuture
import kotlin.system.exitProcess

@Component
class TestHarness(
    @Value("\${n}") private val numRequests: Int,
    private val apiClient: ApiClient,
    private val applicationContext: ApplicationContext,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor,
    private val objectMapper: ObjectMapper
) : ApplicationListener<ApplicationStartedEvent> {
    companion object {
        private val log = LoggerFactory.getLogger(TestHarness::class.java)
    }

    override fun onApplicationEvent(applicationStartedEvent: ApplicationStartedEvent) {
        val futures = Array(numRequests) { CompletableFuture.supplyAsync({ callApi() }, threadPoolTaskExecutor) }
        val responses = CompletableFuture.allOf(*futures).thenApply { futures.map { it.join() } }
        val counts = responses.join().groupingBy { it }.eachCount()

        log.info("Results: {}", objectMapper.configure(INDENT_OUTPUT, true).writeValueAsString(counts))

        exitProcess(exit(applicationContext, { 0 }))
    }

    fun callApi(): String = try {
        apiClient.getTest(0)
    } catch (e: Exception) {
        e.message ?: "Unknown error"
    }
}
