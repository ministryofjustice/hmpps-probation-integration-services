package uk.gov.justice.digital.hmpps.audit.config

import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.Conditional
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.AnnotatedTypeMetadata
import uk.gov.justice.digital.hmpps.datasource.DeliusConnectionProvider

@Configuration
class ConnectionProviderConfig {
    @Bean
    @Conditional(OracleCondition::class)
    fun hibernatePropertiesCustomizer(): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { props: MutableMap<String?, Any?> ->
            props["hibernate.connection.provider_class"] = DeliusConnectionProvider::class.java
        }
    }
}

class OracleCondition : Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata,
    ): Boolean {
        val url = context.environment.getProperty("spring.datasource.url")
        return url?.startsWith("jdbc:oracle") ?: false && !context.environment.acceptsProfiles { it.test("oracle") }
    }
}
