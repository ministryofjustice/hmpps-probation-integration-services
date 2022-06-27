package uk.gov.justice.digital.hmpps.integrations.delius.audit.converter

import uk.gov.justice.digital.hmpps.integrations.delius.audit.AuditedInteraction
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class AuditedInteractionOutcomeConverter : AttributeConverter<AuditedInteraction.Outcome, String> {
    override fun convertToDatabaseColumn(attribute: AuditedInteraction.Outcome): String = when (attribute) {
        AuditedInteraction.Outcome.SUCCESS -> "P"
        AuditedInteraction.Outcome.FAIL -> "F"
    }

    override fun convertToEntityAttribute(dbData: String): AuditedInteraction.Outcome = when (dbData) {
        "P" -> AuditedInteraction.Outcome.SUCCESS
        "F" -> AuditedInteraction.Outcome.FAIL
        else -> throw IllegalArgumentException("Unknown Audited Interaction Outcome")
    }
}
