package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
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

    @Column(name = "description")
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

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false
)
