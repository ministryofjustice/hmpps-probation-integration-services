package uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.OuCodeDeserializer.Companion.OU_CODE_LENGTH
import java.io.IOException
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Component
class DocumentInfoDeserializer<T>(
    clazz: Class<Info>,
) : StdDeserializer<Info>(clazz) {
    @JsonCreator
    constructor() : this(Info::class.java)

    @Throws(IOException::class)
    override fun deserialize(
        jp: JsonParser,
        context: DeserializationContext,
    ): Info {
        val jsonNode: JsonNode = jp.codec.readTree<TreeNode>(jp).get(Info.SOURCE_FILE_NAME_ELEMENT) as JsonNode
        val sourceFileName: String = jsonNode.asText("")

        // Source filename has the following format 146_27072020_2578_B01OB00_ADULT_COURT_LIST_DAILY
        val fileNameParts = sourceFileName.split(FIELD_DELIM.toRegex()).toTypedArray()
        if (fileNameParts.size < 4) {
            log.error("Unable to determine OU code and date of hearing from source file name of {}", sourceFileName)
            return Info(-1, "", LocalDate.of(1970, Month.JANUARY, 1))
        }
        val seq = fileNameParts[0].toLong()
        val courtCode = fileNameParts[3].uppercase(Locale.getDefault())
        val ouCode =
            courtCode.let { code ->
                if (code.length >= OU_CODE_LENGTH) {
                    code.substring(0, OU_CODE_LENGTH)
                } else {
                    courtCode
                }
            }

        return try {
            Info(seq, ouCode, LocalDate.parse(fileNameParts[1], formatter))
        } catch (ex: DateTimeParseException) {
            log.error("Unable to determine OU code and date of hearing from source file name of {}", sourceFileName)
            Info(-1L, "", LocalDate.of(1970, Month.JANUARY, 1))
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
        const val FIELD_DELIM = "_"
        private val formatter = DateTimeFormatter.ofPattern("ddMMyyyy")
    }
}
