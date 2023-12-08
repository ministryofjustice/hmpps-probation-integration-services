package uk.gov.justice.digital.hmpps.integrations.delius.referral.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Lob
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import jakarta.persistence.Version
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "nsi")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "nsi_id_generator", sequenceName = "nsi_id_seq", allocationSize = 1)
class Nsi(
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,
    @ManyToOne
    @JoinColumn(name = "nsi_type_id")
    var type: NsiType,
    @ManyToOne
    @JoinColumn(name = "nsi_status_id")
    var status: NsiStatus,
    @JoinColumn(name = "nsi_outcome_id")
    @ManyToOne
    var outcome: ReferenceData? = null,
    @Column(name = "nsi_status_date")
    var statusDate: ZonedDateTime = ZonedDateTime.now(),
    val referralDate: LocalDate,
    var actualStartDate: ZonedDateTime? = null,
    @Lob
    var notes: String? = null,
    val externalReference: String? = null,
    @Column(name = "intended_provider_id")
    val intendedProviderId: Long? = null,
    @OneToMany(mappedBy = "nsi")
    @SQLRestriction("active_flag = 1")
    private val managers: MutableList<NsiManager> = mutableListOf(),
    val eventId: Long? = null,
    @Column(name = "rqmnt_id")
    val requirementId: Long? = null,
    @Column(name = "rar_count")
    var rarCount: Long? = null,
    @Column(columnDefinition = "number")
    val pendingTransfer: Boolean = false,
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nsi_id_generator")
    @Column(name = "nsi_id")
    val id: Long = 0,
    @Version
    @Column(name = "row_version")
    val version: Long = 0,
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),
    @CreatedBy
    var createdByUserId: Long = 0,
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
    @Column(name = "active_flag", columnDefinition = "number")
    var active: Boolean = true,
    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,
) {
    fun withManager(manager: NsiManager): Nsi {
        managers.add(manager)
        return this
    }

    val manager
        get() = managers.first()

    var actualEndDate: ZonedDateTime? = null
        set(value) {
            field = value
            active = field == null
        }
}

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "nsi_manager")
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "nsi_manager_id_generator", sequenceName = "nsi_manager_id_seq", allocationSize = 1)
class NsiManager(
    @ManyToOne
    @JoinColumn(name = "nsi_id")
    val nsi: Nsi,
    @Column(name = "probation_area_id")
    val providerId: Long,
    @Column(name = "team_id")
    val teamId: Long,
    @Column(name = "staff_id")
    val staffId: Long,
    @Column
    val startDate: ZonedDateTime,
    @Column
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),
    @Column
    @CreatedBy
    var createdByUserId: Long = 0,
    @Column
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),
    @Column
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,
    @Column
    val partitionAreaId: Long = 0,
    @Version
    @Column(name = "row_version")
    val version: Long = 0,
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nsi_manager_id_generator")
    @Column(name = "nsi_manager_id")
    val id: Long = 0,
)

@Entity
@Immutable
@Table(name = "r_nsi_type")
class NsiType(
    val code: String,
    @Id
    @Column(name = "nsi_type_id")
    val id: Long,
)

@Entity
@Immutable
@Table(name = "r_nsi_status")
class NsiStatus(
    val code: String,
    val contactTypeId: Long,
    @Id
    @Column(name = "nsi_status_id")
    val id: Long,
) {
    enum class Code(val value: String) {
        IN_PROGRESS("INPROG"),
        END("COMP"),
    }
}

@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "nsi_status_history")
@SQLRestriction("soft_deleted = 0")
class NsiStatusHistory(
    val nsiId: Long,
    @Column(name = "nsi_status_id")
    val statusId: Long,
    @Column(name = "nsi_status_date")
    val date: ZonedDateTime = ZonedDateTime.now(),
    @Lob
    val notes: String? = null,
    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,
    @Id
    @SequenceGenerator(
        name = "nsi_status_history_id_generator",
        sequenceName = "nsi_status_history_id_seq",
        allocationSize = 1,
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nsi_status_history_id_generator")
    @Column(name = "nsi_status_history_id")
    val id: Long = 0,
    @Version
    @Column(name = "row_version")
    val version: Long = 0,
    @CreatedBy
    var createdByUserId: Long = 0,
    @CreatedDate
    @Column(name = "created_datetime")
    var createdDateTime: ZonedDateTime = ZonedDateTime.now(),
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,
    @LastModifiedDate
    @Column(name = "last_updated_datetime")
    var lastUpdatedDateTime: ZonedDateTime = ZonedDateTime.now(),
)
