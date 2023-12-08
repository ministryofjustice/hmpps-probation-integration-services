package uk.gov.justice.digital.hmpps.audit.converter

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import uk.gov.justice.digital.hmpps.audit.entity.AuditedInteraction

@Converter
class AuditedInteractionOutcomeConverter : AttributeConverter<AuditedInteraction.Outcome, Char> {
    override fun convertToDatabaseColumn(attribute: AuditedInteraction.Outcome): Char =
        when (attribute) {
            AuditedInteraction.Outcome.SUCCESS -> 'P'
            AuditedInteraction.Outcome.FAIL -> 'F'
        }

    override fun convertToEntityAttribute(dbData: Char): AuditedInteraction.Outcome =
        when (dbData) {
            'P' -> AuditedInteraction.Outcome.SUCCESS
            'F' -> AuditedInteraction.Outcome.FAIL
            else -> throw IllegalArgumentException("Unknown Audited Interaction Outcome")
        }
}
