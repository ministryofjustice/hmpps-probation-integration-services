package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd 
        where rd.referenceDataSet.name = 'RELEASE TYPE' 
        and rd.code = :code
        and rd.selectable = true
        """
    )
    fun findReleaseType(code: String): ReferenceData?
}

fun ReferenceDataRepository.getReleaseType(code: String): ReferenceData =
    findReleaseType(code) ?: throw NotFoundException("Release Type", "code", code)
