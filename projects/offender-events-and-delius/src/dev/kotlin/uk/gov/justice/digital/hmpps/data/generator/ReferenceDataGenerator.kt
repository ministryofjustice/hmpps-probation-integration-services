package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

object ReferenceDataGenerator {
    val MAPPA_CATEGORY = ReferenceData(
        id = id(),
        code = "M1",
        description = "MAPPA Category 1",
        datasetId = DatasetGenerator.MAPPA_CATEGORY.id
    )
}
