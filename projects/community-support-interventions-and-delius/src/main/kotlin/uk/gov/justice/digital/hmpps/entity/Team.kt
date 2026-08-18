package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.hibernate.annotations.SQLJoinTableRestriction
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "team")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "telephone")
    val telephone: String? = null,

    @ManyToMany
    @JoinTable(
        name = "team_office_location",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "office_location_id")]
    )
    @SQLJoinTableRestriction("default_team_location_flag = 1")
    val officeLocations: MutableList<OfficeLocation> = mutableListOf()
) {
    val officeLocation: OfficeLocation?
        get() = officeLocations.singleOrNull()
}

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team?
}
