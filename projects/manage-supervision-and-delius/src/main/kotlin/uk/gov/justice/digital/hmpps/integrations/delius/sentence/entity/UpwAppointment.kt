package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Disposal

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

    @JoinColumn(name = "upw_details_id")
    @ManyToOne
    val upwDetails: UpwDetails,
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
        SELECT NVL(sum(ua.MINUTES_CREDITED), 0) 
        FROM UPW_APPOINTMENT ua 
        JOIN UPW_DETAILS ud 
        ON ud.UPW_DETAILS_ID = ua.UPW_DETAILS_ID 
        AND ud.SOFT_DELETED = 0
        JOIN DISPOSAL d 
        ON d.DISPOSAL_ID = ud.DISPOSAL_ID 
        WHERE d.EVENT_ID = :id
        AND ua.ATTENDED = 'Y'
        AND ua.SOFT_DELETED = 0
        """, nativeQuery = true
    )
    fun calculateUnpaidTimeWorked(id: Long): Long
}