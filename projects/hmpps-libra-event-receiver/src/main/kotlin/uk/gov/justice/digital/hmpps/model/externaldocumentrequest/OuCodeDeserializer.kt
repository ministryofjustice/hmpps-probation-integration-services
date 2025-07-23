package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class OuCodeDeserializer<T>(
    clazz: Class<String>,
) : StdDeserializer<String>(clazz) {
    constructor() : this(String::class.java)

    @Throws(IOException::class)
    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): String {
        val jsonNode = jp.codec.readTree<TreeNode>(jp) as JsonNode
        val ouCodeWithRoom: String = jsonNode.asText("")
        if (ouCodeWithRoom.length <= OU_CODE_LENGTH) {
            return ouCodeWithRoom
        }
        val ouCode = ouCodeWithRoom.substring(0, OU_CODE_LENGTH)
        log.trace("Got OU code of {} from input of {}", ouCode, ouCodeWithRoom)
        return ouCode.substring(0, OU_CODE_LENGTH)
    }

    companion object {
        const val OU_CODE_LENGTH = 5
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
