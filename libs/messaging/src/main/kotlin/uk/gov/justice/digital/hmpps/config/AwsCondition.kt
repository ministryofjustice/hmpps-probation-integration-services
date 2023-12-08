package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class AwsCondition : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ) =
        context.environment.activeProfiles.contains("localstack") || !devMessagingConfigured()

    private fun devMessagingConfigured() =
        try {
            Class.forName("uk.gov.justice.digital.hmpps.messaging.HmppsNotificationListener")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
}
