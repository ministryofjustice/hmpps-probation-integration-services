package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
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
    val provider: Provider,

    @OneToMany(mappedBy = "borough")
    val districts: Set<District> = setOf()
)
