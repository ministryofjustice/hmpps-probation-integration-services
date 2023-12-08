package uk.gov.justice.digital.hmpps.audit.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction

@Converter
class AuditedInteractionParamsConverter : AttributeConverter<AuditedInteraction.Parameters, String> {
    override fun convertToDatabaseColumn(attribute: AuditedInteraction.Parameters) =
        attribute.paramPairs().joinToString(",") { "${it.first}='${it.second}'" }

    override fun convertToEntityAttribute(dbData: String?): AuditedInteraction.Parameters {
        var params = AuditedInteraction.Parameters()
        if (dbData !== null) {
            val args =
                dbData.split(",")
                    .filter { it.contains("^.+=.+\$".toRegex()) }
                    .associate {
                        it.substringBefore("=").trim() to
                            it.substringAfter("=")
                                .replace("'", "")
                    }.toMutableMap()
            if (args.isNotEmpty()) params = AuditedInteraction.Parameters(args.toMutableMap())
        }
        return params
    }
}
