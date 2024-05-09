package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
class UpwAppointment(
    @Id
    @Column(name = "upw_appointment_id")
    val id: Long,

    val minutesCredited: Long?,

    @Column(columnDefinition = "char(1)")
    val attended: String?,

    val softDeleted: Long,

    val offenderId: Long,
)

interface UpwAppointmentRepository : JpaRepository<UpwAppointment, Long> {

    @Query(
        """
            SELECT sum(duration) FROM (
                SELECT ua.MINUTES_CREDITED AS duration
                FROM UPW_APPOINTMENT ua
                JOIN OFFENDER o 
                ON o.OFFENDER_ID = ua.OFFENDER_ID 
                JOIN EVENT e 
                ON e.OFFENDER_ID = o.OFFENDER_ID 
                WHERE e.EVENT_ID = :id
                AND e.EVENT_NUMBER = :eventNumber
                AND ua.ATTENDED = 'Y'
                AND ua.SOFT_DELETED = 0)
        """, nativeQuery = true
    )
    fun calculateUnpaidTimeWorked(id: Long, eventNumber: String): Long?
}