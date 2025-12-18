package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData

object DomainEventTypeGenerator {

    val MAPPA_UPDATED = ReferenceData(
        id = 5001L,
        code = "probation-case.mappa-information.updated",
        description = "MAPPA information updated",
        datasetId = DatasetGenerator.DOMAIN_EVENT_TYPE.id
    )

    val MAPPA_DELETED = ReferenceData(
        id = 5002L,
        code = "probation-case.mappa-information.deleted",
        description = "MAPPA information deleted",
        datasetId = DatasetGenerator.DOMAIN_EVENT_TYPE.id
    )
}
