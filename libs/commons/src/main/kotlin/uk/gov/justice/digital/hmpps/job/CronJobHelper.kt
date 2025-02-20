package uk.gov.justice.digital.hmpps.job

import io.opentelemetry.api.trace.Span
import io.sentry.Sentry
import org.springframework.boot.SpringApplication.exit
import org.springframework.context.ApplicationContext
import kotlin.system.exitProcess

object CronJobHelper {
    fun ApplicationContext.runThenExit(job: () -> Unit) {
        try {
            job.invoke()
        } catch (e: Exception) {
            Sentry.captureException(e)
            Span.current().recordException(e)
        } finally {
            exitProcess(exit(this, { 0 }))
        }
    }
}