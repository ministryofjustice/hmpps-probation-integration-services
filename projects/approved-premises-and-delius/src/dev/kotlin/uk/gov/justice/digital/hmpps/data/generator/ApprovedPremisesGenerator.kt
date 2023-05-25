package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.Address
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

object ApprovedPremisesGenerator {
    val DEFAULT = generate(ReferenceDataGenerator.NHC_Q001, AddressGenerator.Q001)
    val NO_STAFF = generate(ReferenceDataGenerator.NHC_Q002, AddressGenerator.Q002)

    fun generate(
        code: ReferenceData,
        address: Address,
        pa: ProbationArea = ProbationAreaGenerator.DEFAULT,
        selectable: Boolean = true,
        id: Long = IdGenerator.getAndIncrement()
    ) = ApprovedPremises(id, code, address, pa, selectable)
}
