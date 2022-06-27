package uk.gov.justice.digital.hmpps.data

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.specto.hoverfly.junit.core.SimulationSource
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.ResourceLoader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

@Component
@Profile("dev", "integration-test", "oracle")
class SimulationBuilder(private val om: ObjectMapper) {
    fun simulationsFromFile(): List<SimulationSource> {
        val parentPath = ResourceLoader.resourceLocationStr

        return Files.walk(Paths.get(parentPath, "simulations"))
            .filter {
                it.isRegularFile() && it.fileName.name.endsWith(".json")
            }.map { simFile ->
                val sim = om.readTree(simFile.toFile()) as ObjectNode
                val pairs = sim["data"]["pairs"].map {
                    if (it["body"] == null || it["body"].isNull) {
                        val filename = it["response"]["bodyFile"].asText()
                        val body = om.readTree(Paths.get("$parentPath/$filename").toFile())
                        val res = it["response"] as ObjectNode
                        res.put("body", body.toString())
                    }
                    it
                }
                val arrNode = om.createArrayNode()
                sim.set<ArrayNode>("pairs", arrNode)
                arrNode.addAll(pairs)

                JsonSource(sim.toString())
            }.toList()
    }

    private data class JsonSource(private val simulation: String) : SimulationSource {
        override fun getSimulation() = simulation
    }
}
