package uk.gov.justice.digital.hmpps.integrations.delius.provider.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.contact.entity.ContactType
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "prison_offender_manager")
class PrisonManager(
    @Id
    @SequenceGenerator(
        name = "prison_manager_id_generator",
        sequenceName = "prison_offender_manager_id_seq",
        allocationSize = 1,
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "prison_manager_id_generator")
    @Column(name = "prison_offender_manager_id", nullable = false)
    val id: Long = 0,
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
    override val staff: Staff,
    @ManyToOne
    @JoinColumn(name = "allocation_team_id", nullable = false)
    override val team: Team,
    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    override val probationArea: ProbationArea,
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "prisonManager", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    val responsibleOfficers: MutableList<ResponsibleOfficer> = mutableListOf(),
    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
) : Manager {
    @Column
    var endDate: ZonedDateTime? = null
        set(value) {
            field = value
            active = value == null
            responsibleOfficer()?.endDate = value
        }

    @Column(name = "active_flag", columnDefinition = "number", nullable = false)
    var active: Boolean = true

    @CreatedBy
    @Column(nullable = false, updatable = false)
    var createdByUserId: Long = 0

    @LastModifiedBy
    @Column(nullable = false)
    var lastUpdatedUserId: Long = 0

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedDate
    @Column(nullable = false)
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    var emailAddress: String? = null

    fun isUnallocated() = staff.code.endsWith("U")

    fun responsibleOfficer(): ResponsibleOfficer? = responsibleOfficers.firstOrNull { it.endDate == null }

    fun makeResponsibleOfficer() {
        responsibleOfficers.add(ResponsibleOfficer(personId, this, date))
    }

    enum class AllocationReasonCode(val value: String, val ctc: ContactType.Code) {
        AUTO("AUT", ContactType.Code.POM_AUTO_ALLOCATION),
        INTERNAL("INA", ContactType.Code.POM_INTERNAL_ALLOCATION),
        EXTERNAL("EXT", ContactType.Code.POM_EXTERNAL_ALLOCATION),
    }
}

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "responsible_officer")
@SequenceGenerator(
    name = "responsible_officer_id_generator",
    sequenceName = "responsible_officer_id_seq",
    allocationSize = 1,
)
class ResponsibleOfficer(
    @Column(name = "offender_id")
    val personId: Long,
    @ManyToOne
    @JoinColumn(name = "PRISON_OFFENDER_MANAGER_ID")
    var prisonManager: PrisonManager?,
    val startDate: ZonedDateTime,
    @Version
    @Column(name = "row_version")
    val version: Long = 0,
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "responsible_officer_id_generator")
    @Column(name = "responsible_officer_id", nullable = false)
    val id: Long = 0,
) {
    @CreatedBy
    var createdByUserId: Long = 0

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now()

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()

    var endDate: ZonedDateTime? = null
}

interface PrisonManagerRepository : JpaRepository<PrisonManager, Long> {
    @Query(
        """
            select pm from PrisonManager pm
            left join fetch pm.responsibleOfficers ro
            where pm.personId = :personId
            and pm.softDeleted = false
            and pm.date <= :date
            and (pm.endDate is null or pm.endDate > :date)
        """,
    )
    fun findActiveManagerAtDate(
        personId: Long,
        date: ZonedDateTime,
    ): PrisonManager?

    @Query(
        """
            select pm from PrisonManager pm
            where pm.personId = :personId
            and pm.softDeleted = false
            and pm.date > :date
            order by pm.date asc
        """,
    )
    fun findFirstManagerAfterDate(
        personId: Long,
        date: ZonedDateTime,
        pageable: Pageable = PageRequest.of(0, 1),
    ): List<PrisonManager>
}
