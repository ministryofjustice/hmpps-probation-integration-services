package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class Borough(
    @Id
    @Column(name = "borough_id")
    val id: Long,

    @Column
    val code: String,

    @Column
    val description: String,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ProbationArea,

    @OneToMany(mappedBy = "borough")
    val districts: Set<District> = setOf()
)
