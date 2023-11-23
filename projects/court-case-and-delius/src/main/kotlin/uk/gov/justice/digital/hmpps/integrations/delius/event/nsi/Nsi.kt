package uk.gov.justice.digital.hmpps.integrations.delius.event.nsi

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate

@Entity
@Table(name = "nsi")
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class Nsi(

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "event_id")
    val eventId: Long,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id")
    val type: NsiType,

    @ManyToOne
    @JoinColumn(name = "nsi_sub_type_id")
    val subType: ReferenceData?,

    @JoinColumn(name = "nsi_outcome_id")
    @ManyToOne
    val outcome: ReferenceData? = null,

    @Column(name = "actual_start_date")
    val actualStartDate: LocalDate? = null,

    @Column(name = "referral_Date")
    val referralDate: LocalDate? = null,

    @Column(name = "nsi_status_date")
    val statusDate: LocalDate? = null,

    @Id
    @Column(name = "nsi_id")
    val id: Long = 0,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "r_nsi_type")
@Entity
class NsiType(
    @Id
    @Column(name = "nsi_type_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String
)

interface NsiRepository : JpaRepository<Nsi, Long> {

    @Query(
        """
            select nsi from Nsi nsi
            where 
            nsi.outcome.code in ('BRE01', 'BRE02', 'BRE03', 'BRE04', 'BRE05', 'BRE06', 'BRE07',
                'BRE08', 'BRE10', 'BRE13', 'BRE14', 'BRE16')
            and nsi.eventId = :eventId
        """
    )
    fun findAllBreachNSIByEventId(eventId: Long): List<Nsi>
}
