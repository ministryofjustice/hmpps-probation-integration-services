package uk.gov.justice.digital.hmpps.job

import io.opentelemetry.api.trace.Span
import io.sentry.Sentry
import org.springframework.boot.SpringApplication.exit
import org.springframework.context.ApplicationContext
import kotlin.system.exitProcess

object CronJobHelper {
    fun ApplicationContext.runThenExit(
        exit: (code: Int) -> Unit = { exitProcess(exit(this, { it })) },
        job: () -> Unit
    ) {
        try {
            job.invoke()
            exit(0)
        } catch (e: Exception) {
            Sentry.captureException(e)
            Span.current().recordException(e)
        } finally {
            exit(1)
        }
    }
}