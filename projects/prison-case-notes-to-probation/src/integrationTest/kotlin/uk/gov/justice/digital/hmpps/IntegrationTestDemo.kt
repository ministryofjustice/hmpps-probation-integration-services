package uk.gov.justice.digital.hmpps

import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.HoverflyMode
import io.specto.hoverfly.junit5.HoverflyExtension
import io.specto.hoverfly.junit5.api.HoverflyConfig
import io.specto.hoverfly.junit5.api.HoverflyCore
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jms.core.JmsTemplate
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.data.SimulationBuilder
import uk.gov.justice.digital.hmpps.integrations.nomis.CaseNoteMessage
import uk.gov.justice.digital.hmpps.listener.CaseNoteMessageWrapper

@ActiveProfiles("test")
@SpringBootTest
@HoverflyCore(
    mode = HoverflyMode.SIMULATE,
    config = HoverflyConfig(adminPort = 8888, proxyPort = 8500, webServer = true)
)
@ExtendWith(HoverflyExtension::class)
class IntegrationTestDemo {

    @Autowired
    private lateinit var simBuilder: SimulationBuilder

    @Autowired
    private lateinit var jmsTemplate: JmsTemplate

    @BeforeEach
    fun setUp(hoverfly: Hoverfly) {
        val sources = simBuilder.simulationsFromFile()
        if (sources.isNotEmpty()) {
            hoverfly.simulate(
                sources.first(),
                *sources.drop(1).toTypedArray()
            )
        }
    }

    @Test
    fun integrationTestDemo() {
        val cnMessage = CaseNoteMessage("GA52214", 1234, "NEG")
        jmsTemplate.convertAndSend(
            "events", CaseNoteMessageWrapper(cnMessage)
        )
    }
}