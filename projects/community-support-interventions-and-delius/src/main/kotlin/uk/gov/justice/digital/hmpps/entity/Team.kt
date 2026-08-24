package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "team")
class Team(
    @Id
    @Column(name = "team_id")
    val id: Long,

    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    @Column(name = "telephone")
    val telephone: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    val district: District,

    @OneToMany(mappedBy = "team")
    @SQLRestriction("default_team_location_flag = 'Y'")
    val officeLocations: MutableList<TeamOfficeLocation> = mutableListOf()
) {
    val officeLocation: OfficeLocation?
        get() = officeLocations.singleOrNull()?.officeLocation
}

interface TeamRepository : JpaRepository<Team, Long> {
    fun findByCode(code: String): Team?
}
