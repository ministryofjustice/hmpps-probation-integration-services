package uk.gov.justice.digital.hmpps.entity.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "team")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,
    @Column(columnDefinition = "char(6)")
    val code: String,
    val description: String,
    @ManyToOne
    @JoinColumn("district_id")
    val localAdminUnit: LocalAdminUnit,
    @OneToMany(mappedBy = "team")
    val staffTeams: List<StaffTeam> = emptyList(),
)