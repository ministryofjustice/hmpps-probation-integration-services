package uk.gov.justice.digital.hmpps.data.generator

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
    val CUSTODY_STATUS = ReferenceData(
        IdGenerator.getAndIncrement(),
        "D",
        "In Custody"
    )
    val CUSTODY = Custody(
        IdGenerator.getAndIncrement(),
        ConvictionEventGenerator.DISPOSAL_2.id,
        "1234BN",
        CUSTODY_STATUS,
        disposal = ConvictionEventGenerator.DISPOSAL_2
    )
    val KEYDATE = KeyDate(
        IdGenerator.getAndIncrement(),
        CUSTODY,
        SED_KEYDATE,
        LocalDate.now().plusYears(5)
    )
    val CUSTODY_1 = Custody(
        IdGenerator.getAndIncrement(),
        ConvictionEventGenerator.DISPOSAL.id,
        "2234BN",
        CUSTODY_STATUS,
        disposal = ConvictionEventGenerator.DISPOSAL
    )
    val KEYDATE_1 = KeyDate(
        IdGenerator.getAndIncrement(),
        CUSTODY_1,
        SED_KEYDATE,
        LocalDate.now().plusYears(5)
    )
}
