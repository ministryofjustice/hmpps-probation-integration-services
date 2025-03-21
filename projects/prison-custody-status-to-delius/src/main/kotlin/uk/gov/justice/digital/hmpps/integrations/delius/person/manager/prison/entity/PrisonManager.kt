package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.prison.entity

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.Team
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "prison_offender_manager")
class PrisonManager(
    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @Column(name = "offender_id", nullable = false)
    val personId: Long,

    @Column(name = "allocation_date", nullable = false)
    val date: ZonedDateTime,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id", nullable = false)
    val allocationReason: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "allocation_team_id", nullable = false)
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: ProbationArea,

    @Column(columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    var active: Boolean = true,

    @Column
    var endDate: ZonedDateTime? = null,

    @CreatedBy
    @Column(nullable = false, updatable = false)
    var createdByUserId: Long = 0,

    @LastModifiedBy
    @Column(nullable = false)
    var lastUpdatedUserId: Long = 0,

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Id
    @SequenceGenerator(
        name = "prison_manager_id_generator",
        sequenceName = "prison_offender_manager_id_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prison_manager_id_generator")
    @Column(name = "prison_offender_manager_id", nullable = false)
    val id: Long = 0,
)

interface PrisonManagerRepository : JpaRepository<PrisonManager, Long> {
    @Query(
        """
            select pm from PrisonManager pm
            where pm.personId = :personId
            and pm.softDeleted = false
            and pm.date <= :date
            and (pm.endDate is null or pm.endDate > :date)
        """
    )
    fun findActiveManagerAtDate(personId: Long, date: ZonedDateTime): PrisonManager?

    @Query(
        """
            select pm from PrisonManager pm
            where pm.personId = :personId
            and pm.softDeleted = false
            and pm.date > :date
            order by pm.date asc
        """
    )
    fun findFirstManagerAfterDate(
        personId: Long,
        date: ZonedDateTime,
        pageable: Pageable = PageRequest.of(0, 1)
    ): List<PrisonManager>
}
