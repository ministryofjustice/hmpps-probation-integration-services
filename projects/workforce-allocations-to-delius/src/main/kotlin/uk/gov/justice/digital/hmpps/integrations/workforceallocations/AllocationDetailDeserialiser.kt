package uk.gov.justice.digital.hmpps.integrations.workforceallocations

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

class AllocationDetailDeserialiser : JsonDeserializer<AllocationDetail>() {
    override fun deserialize(jp: JsonParser, dc: DeserializationContext): AllocationDetail {
        val om = jp.codec
        val node: JsonNode = om.readTree(jp)
        return if (node.has("crn")) {
            om.treeToValue(node, AllocationDetail.PersonAllocationDetail::class.java)
        } else if (node.has("requirementId")) {
            om.treeToValue(node, AllocationDetail.RequirementAllocationDetail::class.java)
        } else if (node.has("eventNumber")) {
            om.treeToValue(node, AllocationDetail.EventAllocationDetail::class.java)
        } else {
            throw IllegalArgumentException("Unexpected response from allocation service.")
        }
    }
}
