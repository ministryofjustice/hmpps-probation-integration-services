package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.data.entity.Custody

object CustodyGenerator {
    val DEFAULT = generate()

    fun generate(
        id: Long = IdGenerator.getAndIncrement(),
    ) = Custody(id, status = ReferenceDataGenerator.CUSTODY_STATUS, disposal = DisposalGenerator.DEFAULT)
}
