package uk.gov.justice.digital.hmpps.config

import io.hawt.jmx.JMXSecurity
import io.hawt.jmx.JMXSecurityMBean
import io.hawt.springboot.HawtioPlugin
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jmx.export.annotation.ManagedResource

@Configuration
class ArtemisConfig {
    @Bean /* This bean replicates the plugin setup in the artemis-plugin:war dependency.
             See the org.apache.activemq.hawtio.plugin.PluginContextListener#contextInitialized method */
    fun hawtioPlugin() = HawtioPlugin(
        "artemis-plugin", "", "",
        arrayOf(
            // This list comes from the web.xml file in the artemis-plugin war
            "js/artemisHelpers.js",
            "js/artemisPlugin.js",
            "js/components/addressSendMessage.js",
            "js/components/addresses.js",
            "js/components/browse.js",
            "js/components/connections.js",
            "js/components/consumers.js",
            "js/components/createAddress.js",
            "js/components/createQueue.js",
            "js/components/deleteAddress.js",
            "js/components/deleteQueue.js",
            "js/components/diagram.js",
            "js/components/help.js",
            "js/components/navigation.js",
            "js/components/preferences.js",
            "js/components/producers.js",
            "js/components/queues.js",
            "js/components/sendMessage.js",
            "js/components/sessions.js",
            "js/components/status.js",
            "js/components/tree.js",
            "js/services/pagination.js",
            "js/services/resource.js",
            "js/services/sendMessageService.js",
            "js/services/toolbar.js"
        )
    )

    @Bean /* This bean bypasses the artemis-plugin authorization when rendering the "Send Message" and "Browse Messages"
             pages, if hawtio authentication is disabled. */
    @ConditionalOnProperty("hawtio.authenticationEnabled", havingValue = "false")
    fun dummyArtemisJMXSecurity(): JMXSecurityMBean =
        @ManagedResource("hawtio:type=security,area=jmx,name=ArtemisJMXSecurity") object : JMXSecurity() {}
}
