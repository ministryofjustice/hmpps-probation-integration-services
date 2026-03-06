package uk.gov.justice.digital.hmpps.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.entity.unpaidwork.UnpaidWorkAppointment

interface UnpaidWorkAppointmentRepository : JpaRepository<UnpaidWorkAppointment, Long> {
    @Query(
        """
        select floor(sum(coalesce(a.minutesCredited, 0)) / 60)
        from UnpaidWorkAppointment a
        where a.details.disposalId = :disposalId 
        """
    )
    fun countHoursAttended(disposalId: Long): Int
}