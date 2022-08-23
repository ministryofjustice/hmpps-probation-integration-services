package uk.gov.justice.digital.hmpps.integrations.delius.custody

import org.hibernate.annotations.Where
import uk.gov.justice.digital.hmpps.integrations.delius.event.Disposal
import uk.gov.justice.digital.hmpps.integrations.delius.institution.Institution
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.release.Release
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Version

@Entity
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

    @Column(name = "status_change_date", nullable = false)
    var statusChangeDate: ZonedDateTime,

    @Column(name = "location_change_date", nullable = false)
    var locationChangeDate: ZonedDateTime,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    val softDeleted: Boolean = false,

    @OneToMany(mappedBy = "custody")
    val releases: MutableList<Release> = ArrayList(),
) {
    fun isInCustody() = status.code in listOf(
        CustodialStatusCode.RECALLED.code,
        CustodialStatusCode.SENTENCED_IN_CUSTODY.code,
        CustodialStatusCode.IN_CUSTODY.code
    )

    fun mostRecentRelease() = releases.maxWithOrNull(compareBy({ it.date }, { it.createdDateTime }))
}
