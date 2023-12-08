package uk.gov.justice.digital.hmpps.integrations.delius.staff.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.Team

@Entity
@Immutable
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long = 0,
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
    @Column
    val forename: String,
    @Column(name = "forename2")
    val middleName: String?,
    @Column
    val surname: String,
    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id", referencedColumnName = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id", referencedColumnName = "team_id")],
    )
    val teams: List<Team> = listOf(),
) {
    fun displayName() =
        when {
            code.endsWith("IAVU") -> "Inactive"
            code.endsWith("U") -> "Unallocated"
            else -> "$surname, ${listOfNotNull(forename, middleName).joinToString(" ")}"
        }
}

interface StaffRepository : JpaRepository<Staff, Long> {
    fun findByCodeAndTeamsId(
        code: String,
        teamId: Long,
    ): Staff?
}

fun StaffRepository.getByCodeAndTeamsId(
    code: String,
    teamId: Long,
) =
    findByCodeAndTeamsId(code, teamId) ?: throw NotFoundException("Staff", "code", code)
