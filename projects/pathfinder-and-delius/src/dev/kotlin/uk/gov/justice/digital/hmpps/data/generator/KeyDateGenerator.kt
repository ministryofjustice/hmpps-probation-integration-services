package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.entity.Custody
import uk.gov.justice.digital.hmpps.entity.KeyDate
import uk.gov.justice.digital.hmpps.entity.ReferenceData
import java.time.LocalDate

object KeyDateGenerator {
    val SED_KEYDATE = ReferenceData(
        IdGenerator.getAndIncrement(),
        "SED",
        "Sentence Expiry Date"
    )
    val CUSTODY = Custody(
        IdGenerator.getAndIncrement(),
        ConvictionEventGenerator.DISPOSAL_2.id
    )
    val KEYDATE = KeyDate(
        IdGenerator.getAndIncrement(),
        CUSTODY,
        SED_KEYDATE,
        LocalDate.now().plusYears(5)
    )
 }