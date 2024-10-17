package uk.gov.justice.digital.hmpps.integrations.delius.entity.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Court
import uk.gov.justice.digital.hmpps.integrations.delius.entity.DatasetCode
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    fun findByCode(code: String): ReferenceData

    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.code = :code
    """
    )
    fun findByCodeAndDatasetCode(code: String, datasetCode: DatasetCode): ReferenceData?
}

fun ReferenceDataRepository.initialAllocationReason() =
    findByCodeAndDatasetCode(ReferenceData.AllocationCode.INITIAL_ALLOCATION.code, DatasetCode.OM_ALLOCATION_REASON)
        ?: throw NotFoundException("Allocation Reason", "code", ReferenceData.AllocationCode.INITIAL_ALLOCATION.code)

interface CourtRepository : JpaRepository<Court, Long> {
    fun findByNationalCourtCode(nationalCourtCode: String): Court
}