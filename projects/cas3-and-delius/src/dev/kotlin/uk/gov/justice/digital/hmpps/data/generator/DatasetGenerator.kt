package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DatasetCode

object DatasetGenerator {
    val ADDRESS_TYPE = Dataset(IdGenerator.getAndIncrement(), DatasetCode.ADDRESS_TYPE)
    val ADDRESS_STATUS = Dataset(IdGenerator.getAndIncrement(), DatasetCode.ADDRESS_STATUS)
}
