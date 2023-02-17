package uk.gov.justice.digital.hmpps.integrations.delius.custody

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinColumns
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Version
import org.hibernate.annotations.Where
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import uk.gov.justice.digital.hmpps.integrations.delius.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.Release
import java.time.ZonedDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
@Where(clause = "soft_deleted = 0")
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
    var institution: Institution,

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false)
    val disposal: Disposal,

    @Column(nullable = false)
    var statusChangeDate: ZonedDateTime,

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
}
