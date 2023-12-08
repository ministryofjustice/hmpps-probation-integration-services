package uk.gov.justice.digital.hmpps.service.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "team")
class Team(
    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,
    val description: String,
    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: ProbationArea,
    @Id
    @Column(name = "team_id")
    val id: Long,
)

@Entity
@Immutable
class ProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,
    @Column
    val description: String,
)
