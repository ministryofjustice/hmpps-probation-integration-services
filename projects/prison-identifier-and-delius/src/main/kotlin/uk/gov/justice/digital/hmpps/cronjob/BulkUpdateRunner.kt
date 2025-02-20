package uk.gov.justice.digital.hmpps.cronjob

import io.opentelemetry.instrumentation.annotations.WithSpan
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.job.CronJobHelper.runThenExit
import uk.gov.justice.digital.hmpps.messaging.Notifier

@Component
@ConditionalOnProperty("bulk.update.enabled")
class BulkUpdateRunner(
    @Value("\${bulk.update.dry-run:true}") private val dryRun: Boolean,
    private val applicationContext: ApplicationContext,
    private val notifier: Notifier,
) : ApplicationListener<ApplicationStartedEvent> {
    @WithSpan("JOB bulk-update-prison-identifiers")
    override fun onApplicationEvent(applicationStartedEvent: ApplicationStartedEvent) = applicationContext.runThenExit {
        notifier.requestPrisonMatching(listOf(), dryRun)
    }
}
