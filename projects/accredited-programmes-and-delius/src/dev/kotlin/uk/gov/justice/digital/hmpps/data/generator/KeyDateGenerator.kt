package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.custody.Custody
import uk.gov.justice.digital.hmpps.entity.sentence.custody.KeyDate
import java.time.LocalDate

object KeyDateGenerator {
    fun generate(custody: Custody, type: ReferenceData, date: LocalDate) = KeyDate(
        id = id(),
        custody = custody,
        type = type,
        date = date,
        softDeleted = false
    )
}
