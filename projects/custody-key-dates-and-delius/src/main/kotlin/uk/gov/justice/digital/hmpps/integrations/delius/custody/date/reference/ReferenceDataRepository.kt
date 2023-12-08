package uk.gov.justice.digital.hmpps.integrations.delius.custody.date.reference

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        SELECT rd FROM ReferenceData rd
        WHERE rd.dataset.code = :datasetCode
        AND rd.code = :code
    """,
    )
    fun findByDatasetAndCode(
        datasetCode: DatasetCode,
        code: String,
    ): ReferenceData?
}

fun ReferenceDataRepository.findKeyDateType(code: String): ReferenceData = findByDatasetAndCode(DatasetCode.KEY_DATE_TYPE, code) ?: throw NotFoundException("KeyDateType", "code", code)
