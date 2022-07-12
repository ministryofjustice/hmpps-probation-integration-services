package uk.gov.justice.digital.hmpps.config

import io.hawt.jmx.JMXSecurity
import io.hawt.jmx.JMXSecurityMBean
import io.hawt.springboot.HawtioPlugin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jmx.export.annotation.ManagedResource

@Configuration
class Config {
    @Bean
    fun HawtioPlugin() = HawtioPlugin(
        "artemis-plugin", "", "",
        arrayOf(
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

    @Bean
    fun dummyArtemisJMXSecurity(): JMXSecurityMBean = DummyArtemisJMXSecurity()
}

@ManagedResource("hawtio:type=security,area=jmx,name=ArtemisJMXSecurity")
class DummyArtemisJMXSecurity : JMXSecurity()
