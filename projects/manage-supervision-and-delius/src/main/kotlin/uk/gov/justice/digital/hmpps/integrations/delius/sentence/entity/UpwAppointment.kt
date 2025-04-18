package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Disposal
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "upw_appointment")
class UpwAppointment(
    @Id
    @Column(name = "upw_appointment_id")
    val id: Long,

    val minutesCredited: Long?,

    @Column(columnDefinition = "char(1)")
    val attended: String?,

    val softDeleted: Long,

    val appointmentDate: LocalDate? = null,

    @JoinColumn(name = "upw_details_id")
    @ManyToOne
    val upwDetails: UpwDetails,

    val upwProjectId: Long? = null,
)

@Entity
@Immutable
class UpwDetails(
    @Id
    @Column(name = "upw_details_id")
    val id: Long,

    @JoinColumn(name = "disposal_id")
    @ManyToOne
    val disposal: Disposal,

    val softDeleted: Long,
)

interface UpwAppointmentRepository : JpaRepository<UpwAppointment, Long> {

    @Query(
        """
            SELECT COALESCE(SUM(u.minutesCredited), 0)
            FROM UpwAppointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :id 
            AND u.softDeleted = 0 
         """
    )
    fun calculateUnpaidTimeWorked(id: Long): Long
}