package uk.gov.justice.digital.hmpps.integrations.delius.custody.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.event.entity.DisposalType.Code.COMMITTAL_PSSR_BREACH
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.entity.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CAN_RECALL_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CAN_RELEASE_STATUSES
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.entity.Release
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@SQLRestriction("soft_deleted = 0")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,

    @ManyToOne
    @JoinColumn(name = "custodial_status_id")
    var status: ReferenceData,

    @ManyToOne
    @JoinColumns(
        JoinColumn(name = "institution_id", referencedColumnName = "institution_id"),
        JoinColumn(name = "establishment", referencedColumnName = "establishment")
    )
    var institution: Institution?,

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal,

    @Column(nullable = false)
    var statusChangeDate: LocalDate,

    @Column
    var locationChangeDate: ZonedDateTime?,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,

    @OneToMany(mappedBy = "custody")
    val releases: MutableList<Release> = ArrayList(),

    @Column(nullable = false, updatable = false)
    @CreatedBy
    var createdByUserId: Long = 0,

    @Column(nullable = false)
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column(nullable = false, updatable = false)
    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(nullable = false)
    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now()
) {
    fun isInCustody() = status.code in listOf(
        CustodialStatusCode.RECALLED.code,
        CustodialStatusCode.SENTENCED_IN_CUSTODY.code,
        CustodialStatusCode.IN_CUSTODY.code
    )

    fun mostRecentRelease() = releases.maxWithOrNull(compareBy({ it.date }, { it.createdDatetime }))

    fun updateLocationAt(
        institution: Institution,
        dateTime: ZonedDateTime,
        historyType: () -> ReferenceData
    ): CustodyHistory? = if (this.institution?.code == institution.code) {
        null
    } else {
        this.institution = institution
        this.locationChangeDate = dateTime
        CustodyHistory(
            date = dateTime,
            type = historyType(),
            detail = institution.description,
            person = disposal.event.person,
            custody = this
        )
    }

    fun updateStatusAt(
        status: ReferenceData,
        dateTime: ZonedDateTime,
        detail: String,
        historyType: () -> ReferenceData
    ): CustodyHistory? = if (this.status.code == status.code) {
        null
    } else {
        this.status = status
        this.statusChangeDate = dateTime.toLocalDate()
        CustodyHistory(
            date = dateTime,
            type = historyType(),
            detail = detail,
            person = disposal.event.person,
            custody = this
        )
    }
}

fun Custody.canBeRecalled(): Boolean {
    val mrr = mostRecentRelease()
    return mrr != null && mrr.recall == null && status.code in CAN_RECALL_STATUSES.map { it.code }
}

fun Custody.canBeReleased() =
    status.code in CAN_RELEASE_STATUSES.map { it.code } && disposal.type.code != COMMITTAL_PSSR_BREACH.value

interface CustodyRepository : JpaRepository<Custody, Long>
