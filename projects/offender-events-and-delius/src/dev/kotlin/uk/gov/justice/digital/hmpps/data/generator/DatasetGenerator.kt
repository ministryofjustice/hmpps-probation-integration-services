package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.Dataset

object DatasetGenerator {
    val DOMAIN_EVENT_TYPE = Dataset(
        id = 5000L,
        name = Dataset.Code.DOMAIN_EVENT_TYPE.value
    )
}
