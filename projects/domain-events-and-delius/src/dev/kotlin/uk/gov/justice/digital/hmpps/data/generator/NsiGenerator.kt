package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Event
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.entity.StandardReferenceList
import java.time.LocalDate

object NsiGenerator {

    const val NSI_ID = 2500000986
    val EVENT = Event(IdGenerator.getAndIncrement(), "1")
    val OUTCOME = StandardReferenceList(IdGenerator.getAndIncrement(), "BRE01", "Revoked & Re- Sentenced")
    val NSI_STATUS = StandardReferenceList(IdGenerator.getAndIncrement(), "208", "DTTO - Low Intensity")
    val REFERRAL_DATE: LocalDate = LocalDate.of(2022, 1, 31)

    val BREACH_DETAILS_NSI = Nsi(
        NSI_ID,
        REFERRAL_DATE,
        OUTCOME,
        NSI_STATUS,
        EVENT,
    )
}
