package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Disposal
import java.time.ZonedDateTime

@Entity
@Immutable
@SQLRestriction("soft_deleted = 0")
class Custody(
    @Id
    @Column(name = "custody_id")
    val id: Long,

    @Column(name = "disposal_id")
    val disposalId: Long,

    @OneToOne
    @JoinColumn(name = "disposal_id", updatable = false, insertable = false)
    val disposal: Disposal,

    @OneToMany(mappedBy = "custody")
    val releases: List<Release> = listOf(),

    @Column(columnDefinition = "number")
    val softDeleted: Boolean
) {
    fun mostRecentRelease() = releases.maxWithOrNull(compareBy({ it.date }, { it.createdDateTime }))
}

interface CustodyRepository : JpaRepository<Custody, Long> {
    fun findAllByDisposalId(id: Long): List<Custody>
}

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
class Release(
    @Id
    @Column(name = "release_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "custody_id")
    val custody: Custody,

    @Column(name = "actual_release_date")
    val date: ZonedDateTime,

    @Column(name = "created_datetime")
    val createdDateTime: ZonedDateTime,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean
)
