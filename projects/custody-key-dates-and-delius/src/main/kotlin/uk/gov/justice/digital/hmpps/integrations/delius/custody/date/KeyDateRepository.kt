package uk.gov.justice.digital.hmpps.integrations.delius.custody.date

import org.springframework.data.jpa.repository.JpaRepository

interface KeyDateRepository : JpaRepository<KeyDate, Long> {
    fun deleteByCustodyDisposalIdAndTypeCode(disposalId: Long, typeCode: String): Long
}
