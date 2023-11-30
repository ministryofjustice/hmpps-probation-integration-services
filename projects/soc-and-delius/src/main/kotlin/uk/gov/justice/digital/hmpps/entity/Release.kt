package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Table(name = "release")
@Entity
@SQLRestriction("soft_deleted = 0")
class DetailRelease(
    @Id
    @Column(name = "release_id")
    val id: Long,

    @Column
    val custodyId: Long,

    @ManyToOne
    @JoinColumn(name = "institution_id")
    val institution: Institution? = null,

    @OneToOne(mappedBy = "release")
    val recall: Recall? = null,

    @ManyToOne
    @JoinColumn(name = "release_type_id")
    val releaseType: ReferenceData,

    @Column(name = "actual_release_date")
    val date: LocalDate,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Table(name = "r_institution")
@Entity
class Institution(
    @Id
    @Column(name = "institution_id")
    val id: Long,

    @Column(name = "institution_name")
    val name: String
)

@Immutable
@Table(name = "recall")
@Entity
@SQLRestriction("soft_deleted = 0")
class Recall(
    @Id
    @Column(name = "recall_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "release_id")
    val release: DetailRelease,

    @Column(name = "recall_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "recall_reason_id")
    val reason: RecallReason,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)

@Immutable
@Entity
@Table(name = "r_recall_reason")
class RecallReason(
    @Id
    @Column(name = "recall_reason_id")
    val id: Long,

    @Column(nullable = false)
    val code: String,

    @Column(nullable = false)
    val description: String
)

interface DetailReleaseRepository : JpaRepository<DetailRelease, Long> {
    @EntityGraph(attributePaths = ["recall", "institution"])
    fun findFirstByCustodyIdOrderByDateDesc(custodyId: Long): DetailRelease?
}
