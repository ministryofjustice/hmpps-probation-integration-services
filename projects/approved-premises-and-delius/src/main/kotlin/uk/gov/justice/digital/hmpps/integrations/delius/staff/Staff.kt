package uk.gov.justice.digital.hmpps.integrations.delius.staff

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.entity.ApprovedPremises
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.team.Team

@Entity
@Immutable
class Staff(
    @Id
    @Column(name = "staff_id")
    val id: Long = 0,
    @Column(name = "officer_code", columnDefinition = "char(7)")
    val code: String,
    @ManyToOne
    @JoinColumn(name = "staff_grade_id")
    val grade: ReferenceData?,
    @Column
    val forename: String,
    @Column(name = "forename2")
    val middleName: String?,
    @Column
    val surname: String,
    @OneToOne(mappedBy = "staff")
    val user: StaffUser? = null,
    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "team_id")],
    )
    val teams: List<Team>,
    @ManyToMany
    @JoinTable(
        name = "r_approved_premises_staff",
        joinColumns = [JoinColumn(name = "staff_id")],
        inverseJoinColumns = [JoinColumn(name = "approved_premises_id")],
    )
    val approvedPremises: List<ApprovedPremises>,
)

@Entity
@Immutable
@Table(name = "user_")
class StaffUser(
    @OneToOne
    @JoinColumn(name = "staff_id")
    val staff: Staff? = null,
    @Column(name = "distinguished_name")
    val username: String,
    @Id
    @Column(name = "user_id")
    val id: Long,
)
