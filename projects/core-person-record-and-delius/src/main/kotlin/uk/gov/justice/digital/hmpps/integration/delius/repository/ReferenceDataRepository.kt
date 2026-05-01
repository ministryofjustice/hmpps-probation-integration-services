package uk.gov.justice.digital.hmpps.integration.delius.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.entity.Dataset
import uk.gov.justice.digital.hmpps.integration.delius.entity.ReferenceData

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.code = :code
        """
    )
    fun findByCodeAndDatasetCode(code: String, datasetCode: String): ReferenceData?

    fun getByDataset(code: String, datasetCode: String): ReferenceData =
        findByCodeAndDatasetCode(code, datasetCode) ?: throw NotFoundException(datasetCode, "code", code)

    fun getAddressType(code: String) = getByDataset(code, Dataset.ADDRESS_TYPE)
    fun getAddressStatus(code: String) = getByDataset(code, Dataset.ADDRESS_STATUS)
}
