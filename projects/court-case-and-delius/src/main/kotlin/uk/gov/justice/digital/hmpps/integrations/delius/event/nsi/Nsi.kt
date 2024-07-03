package uk.gov.justice.digital.hmpps.integrations.delius.event.nsi

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.event.conviction.entity.Requirement
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaEntity
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "nsi")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Nsi(

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "event_id")
    val eventId: Long,

    @ManyToOne
    @JoinColumn(name = "nsi_type_id")
    val type: NsiType,

    @OneToOne
    @JoinColumn(name = "nsi_status_id")
    val nsiStatus: NsiStatus,

    @Column(name = "referral_Date")
    val referralDate: LocalDate,

    @Column(name = "nsi_status_date")
    val statusDate: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "nsi_sub_type_id")
    val subType: ReferenceData?,

    @JoinColumn(name = "nsi_outcome_id")
    @ManyToOne
    val outcome: ReferenceData? = null,

    @Column(name = "actual_start_date")
    val actualStartDate: LocalDate? = null,

    @Column(name = "expected_start_date")
    val expectedStartDate: LocalDate? = null,

    @Column(name = "actual_end_date")
    val actualEndDate: LocalDate? = null,

    @Column(name = "expected_end_date")
    val expectedEndDate: LocalDate? = null,

    @Column(name = "rqmnt_id")
    val requirementId: Long?,

    @Column(name = "length")
    val length: Long?,

    @OneToMany(mappedBy = "nsi")
    val managers: List<NsiManager> = listOf(),

    @Id
    @Column(name = "nsi_id")
    val id: Long = 0,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @OneToOne
    @JoinColumn(name = "rqmnt_id", updatable = false, insertable = false)
    val requirement: Requirement? = null,
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

@Entity
@Immutable
@Table(name = "r_nsi_status")
class NsiStatus(
    @Id
    @Column(name = "nsi_status_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String
)

@Entity
@Table(name = "nsi_manager")
@Immutable
class NsiManager (
    @Id
    @Column(name = "nsi_manager_id")
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "nsi_id")
    val nsi: Nsi,

    @Column(name = "start_date")
    val startDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,
//    @ManyToOne
//    @JoinColumn(name = "staff_id")
//    val staff: Staff,
//
    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationAreaEntity,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false
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

    fun findByPersonIdAndEventIdAndTypeCodeIn(personId: Long, eventId: Long, codes: List<String>): List<Nsi>
}

