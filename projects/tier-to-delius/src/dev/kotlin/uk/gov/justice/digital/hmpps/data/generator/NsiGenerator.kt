package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.nsi.entity.Nsi
import uk.gov.justice.digital.hmpps.integrations.delius.nsi.entity.NsiEvent
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate

object NsiGenerator {
    fun generate(
        personId: Long,
        event: NsiEvent? = null,
        referralDate: LocalDate,
        outcome: ReferenceData? = null,
        softDeleted: Boolean = false,
        id: Long = IdGenerator.getAndIncrement()
    ) = Nsi(id, personId, event, referralDate, outcome, softDeleted)
}
