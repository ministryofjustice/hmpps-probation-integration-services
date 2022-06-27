package uk.gov.justice.digital.hmpps.integrations.delius.audit.converter

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
        val args = dbData?.split(",")
            ?.filter { it.contains("^.+=.+\$".toRegex()) }
            ?.map { it.substringBefore("=").trim() to it.substringAfter("=").replace("'", "") }
            ?.toTypedArray() ?: arrayOf()
        return if (args.isNotEmpty()) AuditedInteraction.Parameters(*args)
        else AuditedInteraction.Parameters()
    }
}
