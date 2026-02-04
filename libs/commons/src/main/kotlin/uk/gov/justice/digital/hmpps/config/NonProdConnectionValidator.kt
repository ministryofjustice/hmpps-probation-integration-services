package uk.gov.justice.digital.hmpps.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.logging.Logger
import uk.gov.justice.digital.hmpps.logging.Logger.logger

@ConditionalOnProperty("spring.datasource.url")
@Configuration
class NonProdConnectionValidator(private val dsp: DataSourceProperties) : HibernatePropertiesCustomizer {
    override fun customize(hibernateProperties: Map<String, Any>) {
        if (dsp.isForPreprodOrProd()) {
            Logger.logger().warn("Application connecting to ${dsp.url}")
            hibernateProperties["hibernate.hbm2ddl.auto"]?.also {
                require(it == "validate") {
                    "Application attempted to connect to preprod or prod with auto ddl active."
                }
            }
        }
    }
}

private fun DataSourceProperties.isForPreprodOrProd() =
    url?.contains("PRDNDA") == true || url?.contains("PRENDA") == true