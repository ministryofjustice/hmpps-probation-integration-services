package uk.gov.justice.digital.hmpps.cronjob

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication.exit
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.messaging.Notifier
import kotlin.system.exitProcess

@Component
@ConditionalOnProperty("bulk.update.enabled")
class BulkUpdateRunner(
    @Value("\${bulk.update.dry-run:true}") private val dryRun: Boolean,
    private val applicationContext: ApplicationContext,
    private val notifier: Notifier,
) : ApplicationListener<ApplicationStartedEvent> {

    override fun onApplicationEvent(applicationStartedEvent: ApplicationStartedEvent) {
        notifier.requestPrisonMatching(listOf(), dryRun)
        exitProcess(exit(applicationContext, { 0 }))
    }
}
