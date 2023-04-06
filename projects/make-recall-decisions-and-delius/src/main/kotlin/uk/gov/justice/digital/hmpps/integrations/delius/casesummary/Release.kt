package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Table(name = "release")
@Entity(name = "CaseSummaryRelease")
@Where(clause = "soft_deleted = 0")
class Release(
    @Id
    @Column(name = "release_id")
    val id: Long,

    @Column
    val custodyId: Long,

    @Column(name = "actual_release_date")
    val date: LocalDate,

    @OneToOne(mappedBy = "release")
    val recall: Recall? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "recall")
@Entity(name = "CaseSummaryRecall")
@Where(clause = "soft_deleted = 0")
class Recall(
    @Id
    @Column(name = "recall_id")
    val id: Long,

    @Column(name = "recall_date")
    val date: LocalDate,

    @OneToOne
    @JoinColumn(name = "release_id")
    val release: Release,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

interface CaseSummaryReleaseRepository : JpaRepository<Release, Long> {
    fun findFirstByCustodyIdOrderByDateDesc(custodyId: Long): Release?
}
