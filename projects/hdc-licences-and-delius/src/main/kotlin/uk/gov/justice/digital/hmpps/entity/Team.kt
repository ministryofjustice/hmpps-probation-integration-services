package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(columnDefinition = "char(6)")
    val code: String,

    @Column
    val description: String,

    @Column
    val telephone: String? = null,

    @Column
    val emailAddress: String? = null,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationArea
)
