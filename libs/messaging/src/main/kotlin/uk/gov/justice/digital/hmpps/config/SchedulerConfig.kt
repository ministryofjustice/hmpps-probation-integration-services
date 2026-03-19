package uk.gov.justice.digital.hmpps.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConditionalOnExpression("\${messaging.consumer.enabled:true} and '\${messaging.consumer.queue:}' != ''")
class SchedulerConfig
