package uk.gov.justice.digital.hmpps.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConditionalOnProperty("poller.enabled")
class SchedulingConfig