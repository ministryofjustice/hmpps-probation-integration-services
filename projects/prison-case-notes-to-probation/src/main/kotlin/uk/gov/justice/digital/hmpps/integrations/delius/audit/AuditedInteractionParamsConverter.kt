package uk.gov.justice.digital.hmpps.integrations.delius.audit

import java.util.concurrent.atomic.AtomicInteger
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class AuditedInteractionParamsConverter : AttributeConverter<AuditedInteraction.Parameters, String> {

    private val atomicInteger: AtomicInteger = AtomicInteger(0)

    override fun convertToDatabaseColumn(attribute: AuditedInteraction.Parameters) =
        attribute.paramPairs().joinToString { "${it.first}='${it.second}'" }

    override fun convertToEntityAttribute(dbData: String?): AuditedInteraction.Parameters {
        return AuditedInteraction.Parameters(
            *dbData?.split(",")
                ?.map { it.substringBefore("=") to it.substringAfter("=").replace("'", "") }
                ?.toTypedArray()
                ?: arrayOf()
        )
    }
}
