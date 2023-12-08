package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "staff")
class StaffEntity(
    @Id
    @Column(name = "staff_id")
    val id: Long,
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
    @Column
    val forename: String,
    @Column
    val forename2: String? = null,
    @Column
    val surname: String,
    @OneToOne(mappedBy = "staff")
    val user: User? = null,
    @OneToMany(mappedBy = "staff")
    val communityManagers: Set<CommunityManagerEntity> = setOf(),
    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")],
    )
    val teams: List<Team>,
) {
    fun forenames() = listOfNotNull(forename, forename2).joinToString(" ")
}
