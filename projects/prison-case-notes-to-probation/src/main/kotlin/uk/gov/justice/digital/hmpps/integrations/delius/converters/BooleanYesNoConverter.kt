package uk.gov.justice.digital.hmpps.integrations.delius.converters

import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter
class BooleanYesNoConverter : AttributeConverter<Boolean, Char> {
    override fun convertToDatabaseColumn(attribute: Boolean): Char = if (attribute) 'Y' else 'N'

    override fun convertToEntityAttribute(dbData: Char?) = dbData?.equals('Y') ?: false
}