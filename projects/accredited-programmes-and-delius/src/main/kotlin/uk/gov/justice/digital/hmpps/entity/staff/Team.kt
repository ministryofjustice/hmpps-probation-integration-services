package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Entity
@Immutable
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val localAdminUnit: LocalAdminUnit,
)