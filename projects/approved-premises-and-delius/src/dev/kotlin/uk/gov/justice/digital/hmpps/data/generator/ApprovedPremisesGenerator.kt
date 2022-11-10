package uk.gov.justice.digital.hmpps.data.generator

import IdGenerator
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData

object ApprovedPremisesGenerator {
    val DEFAULT = generate("Q001")
    val NO_STAFF = generate("Q002")

    fun generate(code: String) = ApprovedPremises(
        id = IdGenerator.getAndIncrement(),
        code = ReferenceData(
            id = IdGenerator.getAndIncrement(),
            code = code,
            description = "Description of $code"
        )
    )
}
