package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd 
        where rd.referenceDataSet.name = 'RELEASE TYPE' 
        and rd.code = :code
        and rd.selectable = true
        """
    )
    fun getReleaseType(code: String): ReferenceData
}
