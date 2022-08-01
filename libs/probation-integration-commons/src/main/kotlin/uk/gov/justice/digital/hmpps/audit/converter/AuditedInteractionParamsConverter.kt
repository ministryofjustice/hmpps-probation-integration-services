package uk.gov.justice.digital.hmpps.audit.converter

import uk.gov.justice.digital.hmpps.integrations.delius.audit.AuditedInteraction
import java.util.concurrent.atomic.AtomicInteger
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class AuditedInteractionParamsConverter : AttributeConverter<AuditedInteraction.Parameters, String> {

    private val atomicInteger: AtomicInteger = AtomicInteger(0)

    override fun convertToDatabaseColumn(attribute: AuditedInteraction.Parameters) =
        attribute.paramPairs().joinToString(",") { "${it.first}='${it.second}'" }

    override fun convertToEntityAttribute(dbData: String?): AuditedInteraction.Parameters {
        var params = AuditedInteraction.Parameters()
        if (dbData !== null) {
            val args = dbData.split(",")
                .filter { it.contains("^.+=.+\$".toRegex()) }
                .associate { it.substringBefore("=").trim() to it.substringAfter("=").replace("'", "") }
            if (args.isNotEmpty()) params = AuditedInteraction.Parameters(args)
        }
        return params
    }
}
