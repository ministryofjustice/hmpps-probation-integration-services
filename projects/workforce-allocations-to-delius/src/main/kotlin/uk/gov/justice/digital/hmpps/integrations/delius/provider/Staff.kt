package uk.gov.justice.digital.hmpps.integrations.delius.provider

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.StaffUser
import java.time.ZonedDateTime

@Immutable
@Entity
class Staff(

    @Id
    @Column(name = "staff_id")
    val id: Long = 0,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,

    val forename: String,
    val surname: String,

    @Column(name = "FORENAME2", length = 35)
    val middleName: String? = null,

    @Column(name = "end_date")
    val endDate: ZonedDateTime? = null,

    @ManyToOne
    @JoinColumn(name = "staff_grade_id")
    val grade: ReferenceData? = null,

    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,

    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")]
    )
    val teams: List<Team> = mutableListOf(),
) {
    @Transient
    val displayName = listOfNotNull(forename, middleName, surname).joinToString(" ")
}
