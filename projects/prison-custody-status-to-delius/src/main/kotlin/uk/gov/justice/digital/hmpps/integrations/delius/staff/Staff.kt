package uk.gov.justice.digital.hmpps.integrations.delius.staff

import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany

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
    val teams: MutableList<Team> = mutableListOf()
) {
    fun displayName() = when {
        code.endsWith("IAVU") -> "Inactive"
        code.endsWith("U") -> "Unallocated"
        else -> "$surname, ${listOfNotNull(forename, middleName).joinToString(" ")}"
    }
}
