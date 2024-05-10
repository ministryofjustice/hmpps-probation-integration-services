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
        "SELECT SUM(u.minutesCredited) " +
            "FROM UpwAppointment u"
    )
    fun calculateUnpaidTimeWorked(id: Long): Long
}