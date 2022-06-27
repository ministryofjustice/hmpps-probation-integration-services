package uk.gov.justice.digital.hmpps.integrations.delius.audit.converter

import uk.gov.justice.digital.hmpps.integrations.delius.audit.AuditedInteraction
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class AuditedInteractionOutcomeConverter : AttributeConverter<AuditedInteraction.Outcome, Char> {
    override fun convertToDatabaseColumn(attribute: AuditedInteraction.Outcome): Char = when (attribute) {
        AuditedInteraction.Outcome.SUCCESS -> 'P'
        AuditedInteraction.Outcome.FAIL -> 'F'
    }

    override fun convertToEntityAttribute(dbData: Char): AuditedInteraction.Outcome = when (dbData) {
        'P' -> AuditedInteraction.Outcome.SUCCESS
        'F' -> AuditedInteraction.Outcome.FAIL
        else -> throw IllegalArgumentException("Unknown Audited Interaction Outcome")
    }
}
