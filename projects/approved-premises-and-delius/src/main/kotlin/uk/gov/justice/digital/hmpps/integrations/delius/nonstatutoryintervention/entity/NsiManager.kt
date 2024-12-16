package uk.gov.justice.digital.hmpps.integrations.delius.nonstatutoryintervention.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.staff.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
class NsiManager(
    @Id
    @SequenceGenerator(name = "nsi_manager_id_generator", sequenceName = "nsi_manager_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "nsi_manager_id_generator")
    @Column(name = "nsi_manager_id")
    val id: Long = 0,

    @Version
    @Column(name = "row_version")
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "nsi_id")
    val nsi: Nsi,

    @ManyToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationArea,

    @Column
    val startDate: ZonedDateTime,

    @Column
    val endDate: ZonedDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "transfer_reason_id")
    val transferReason: TransferReason,

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

    @Column
    val partitionAreaId: Long = 0, // this is no longer used but the Delius database still requires it to be populated

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface NsiManagerRepository : JpaRepository<NsiManager, Long>

@Entity
@Immutable
@Table(name = "r_transfer_reason")
class TransferReason(
    @Id
    @Column(name = "transfer_reason_id")
    val id: Long,

    @Column(name = "code")
    val code: String
)

interface TransferReasonRepository : JpaRepository<TransferReason, Long> {
    fun findByCode(code: String): TransferReason?
}

fun TransferReasonRepository.getNsiTransferReason(code: String = "NSI") =
    findByCode(code) ?: throw NotFoundException("Transfer reason", "code", code)
