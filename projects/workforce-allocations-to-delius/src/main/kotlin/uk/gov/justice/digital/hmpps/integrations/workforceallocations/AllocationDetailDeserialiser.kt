package uk.gov.justice.digital.hmpps.integrations.workforceallocations

import tools.jackson.core.JsonParser
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ValueDeserializer
import tools.jackson.module.kotlin.jacksonTypeRef

class AllocationDetailDeserialiser : ValueDeserializer<AllocationDetail>() {
    override fun deserialize(jsonParser: JsonParser, dc: DeserializationContext): AllocationDetail {
        val objectReadContext = jsonParser.objectReadContext()
        val node: JsonNode = objectReadContext.readTree(jsonParser)
        return when {
            node.has("crn") -> objectReadContext.readValue(jsonParser, jacksonTypeRef<AllocationDetail.PersonAllocation>())
            node.has("requirementId") -> objectReadContext.readValue(jsonParser, jacksonTypeRef<AllocationDetail.RequirementAllocation>())
            node.has("eventNumber") -> objectReadContext.readValue(jsonParser, jacksonTypeRef<AllocationDetail.EventAllocation>())
            else -> throw IllegalArgumentException("Unexpected response from allocation service.")
        }
    }
}
