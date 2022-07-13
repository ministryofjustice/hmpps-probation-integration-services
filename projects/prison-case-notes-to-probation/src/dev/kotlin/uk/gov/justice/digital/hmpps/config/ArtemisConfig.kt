package uk.gov.justice.digital.hmpps.config

import io.hawt.jmx.JMXSecurity
import io.hawt.jmx.JMXSecurityMBean
import io.hawt.springboot.HawtioPlugin
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.jmx.export.annotation.ManagedResource

@Configuration
class ArtemisConfig {
    @Bean /* This bean replicates the plugin setup in the artemis-plugin:war dependency. The referenced scripts are
             exposed to the Hawtio UI using the `spring.web.resources.static-locations` property in application.yml.
             See the org.apache.activemq.hawtio.plugin.PluginContextListener#contextInitialized method */
    fun hawtioPlugin() = HawtioPlugin(
        "artemis-plugin", "", "",
        PathMatchingResourcePatternResolver()
            .getResources("classpath:/plugin/**")
            .map { (it as ClassPathResource).path.removePrefix("plugin/") }
            .filter { it.endsWith(".js") }
            .toTypedArray()
    )

    @Bean /* This bean bypasses the artemis-plugin authorization when rendering the "Send Message" and "Browse Messages"
             pages, if hawtio authentication is disabled. */
    @ConditionalOnProperty("hawtio.authenticationEnabled", havingValue = "false")
    fun dummyArtemisJMXSecurity(): JMXSecurityMBean =
        @ManagedResource("hawtio:type=security,area=jmx,name=ArtemisJMXSecurity") object : JMXSecurity() {}
}
