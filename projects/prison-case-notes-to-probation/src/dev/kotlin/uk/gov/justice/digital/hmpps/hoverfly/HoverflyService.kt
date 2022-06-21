package uk.gov.justice.digital.hmpps.hoverfly

import io.specto.hoverfly.junit.core.Hoverfly
import io.specto.hoverfly.junit.core.HoverflyConfig
import io.specto.hoverfly.junit.core.HoverflyMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.data.SimulationBuilder
import javax.annotation.PostConstruct

@Service
@ConditionalOnProperty("hoverfly.run", havingValue = "true", matchIfMissing = false)
class HoverflyService(
    private val simulationBuilder: SimulationBuilder
) {
    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    private val hoverfly = Hoverfly(
        HoverflyConfig
            .localConfigs()
            .proxyPort(8500)
            .adminPort(8888)
            .asWebServer(),
        HoverflyMode.SIMULATE
    )

    @PostConstruct
    fun init() {
        log.warn("Starting hoverfly instance for dev ...")
        hoverfly.start()

        val sources = simulationBuilder.simulationsFromFile()
        if (sources.isNotEmpty()) {
            hoverfly.simulate(
                sources.first(),
                *sources.drop(1).toTypedArray()
            )
        }
    }
}
