package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.data.generator.IdGenerator.id
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.Dataset

object DatasetGenerator {
    val DOMAIN_EVENT_TYPE = Dataset(
        id = id(),
        name = Dataset.Code.DOMAIN_EVENT_TYPE.value
    )
    val MAPPA_CATEGORY = Dataset(
        id = id(),
        name = "MAPPA CATEGORY"
    )
}
