package uk.gov.justice.digital.hmpps.data

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.specto.hoverfly.junit.core.SimulationSource
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.pathString

@Component
@Profile("dev", "integration-test")
class SimulationBuilder(private val objectMapper: ObjectMapper) {
    fun simulationsFromFile(): List<SimulationSource> {
        val simulationsPath = Paths.get(javaClass.getResource("/simulations")!!.path)
        val parentPath = simulationsPath.parent.pathString

        return Files.walk(simulationsPath)
            .filter {
                it.isRegularFile() && it.fileName.name.endsWith(".json")
            }.map { simFile ->
                val sim = objectMapper.readTree(simFile.toFile()) as ObjectNode
                val pairs = sim["data"]["pairs"].map {
                    if (it["body"] == null || it["body"].isNull) {
                        val filename = it["response"]["bodyFile"].asText()
                        val body = objectMapper.readTree(Paths.get(parentPath, filename).toFile())
                        val res = it["response"] as ObjectNode
                        res.put("body", body.toString())
                    }
                    it
                }
                val arrNode = objectMapper.createArrayNode()
                sim.set<ArrayNode>("pairs", arrNode)
                arrNode.addAll(pairs)

                JsonSource(sim.toString())
            }.toList()
    }

    private data class JsonSource(private val simulation: String) : SimulationSource {
        override fun getSimulation() = simulation
    }
}
