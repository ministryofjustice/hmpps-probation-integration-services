package uk.gov.justice.digital.hmpps.service.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class UpwAppointment(
    @Id
    @Column(name = "upw_appointment_id")
    val id: Long,
    val minutesCredited: Long?,
    @Column(columnDefinition = "char(1)")
    val attended: String?,
    val softDeleted: Boolean = false,
    @JoinColumn(name = "upw_details_id")
    @ManyToOne
    val upwDetails: UpwDetails,
)

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class UpwDetails(
    @Id
    @Column(name = "upw_details_id")
    val id: Long,

    @JoinColumn(name = "disposal_id")
    @ManyToOne
    val disposal: Disposal,

    val softDeleted: Boolean = false,
)

interface UpwAppointmentRepository : JpaRepository<UpwAppointment, Long> {

    @Query(
        """
            SELECT COALESCE(SUM(u.minutesCredited), 0)
            FROM UpwAppointment u 
            JOIN  u.upwDetails upd 
            JOIN  upd.disposal d 
            WHERE d.id = :disposalid
         """
    )
    fun calculateUnpaidTimeWorked(disposalid: Long): Int
}