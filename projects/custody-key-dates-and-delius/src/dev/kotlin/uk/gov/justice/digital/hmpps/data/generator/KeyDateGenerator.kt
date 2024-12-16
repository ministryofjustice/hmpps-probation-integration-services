package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.KeyDate
import uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference.ReferenceData
import java.time.LocalDate

object KeyDateGenerator {

    fun generate(custody: Custody, type: ReferenceData, date: LocalDate, softDeleted: Boolean) =
        KeyDate(custody, type, date).also { it.softDeleted = softDeleted }
}
