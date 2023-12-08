package uk.gov.justice.digital.hmpps.integrations.delius.casesummary

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Table(name = "release")
@Entity(name = "CaseSummaryRelease")
@SQLRestriction("soft_deleted = 0")
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
    @ManyToOne
    @JoinColumn(name = "institution_id")
    val institution: Institution? = null,
    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,
)

@Immutable
@Table(name = "recall")
@Entity(name = "CaseSummaryRecall")
@SQLRestriction("soft_deleted = 0")
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
    val softDeleted: Boolean = false,
)

@Immutable
@Table(name = "r_institution")
@Entity(name = "CaseSummaryInstitution")
class Institution(
    @Id
    @Column(name = "institution_id")
    val id: Long,
    @Column(name = "institution_name")
    val name: String?,
    @Column
    val description: String,
)

interface CaseSummaryReleaseRepository : JpaRepository<Release, Long> {
    @EntityGraph(attributePaths = ["recall", "institution"])
    fun findFirstByCustodyIdOrderByDateDesc(custodyId: Long): Release?
}
