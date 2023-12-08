package uk.gov.justice.digital.hmpps.integrations.delius.caseload

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
data class Caseload(
    @Id
    @Column(name = "caseload_id")
    private val id: Long,
    @ManyToOne
    @JoinColumn(name = "offender_id")
    private val person: CaseloadPerson,
    @ManyToOne
    @JoinColumn(name = "trust_provider_team_id")
    private val team: CaseloadTeam,
)

@Entity
@Immutable
@Table(name = "offender")
class CaseloadPerson(
    @Id
    @Column(name = "offender_id")
    private val id: Long,
    @Column(columnDefinition = "char(7)")
    private val crn: String,
)

@Entity
@Immutable
@Table(name = "team")
class CaseloadTeam(
    @Id
    @Column(name = "team_id")
    private val id: Long,
    @Column(name = "code", columnDefinition = "char(6)")
    private val code: String,
    @ManyToMany
    @JoinTable(
        name = "staff_team",
        joinColumns = [JoinColumn(name = "team_id")],
        inverseJoinColumns = [JoinColumn(name = "staff_id")],
    )
    private val staff: List<CaseloadStaff>,
)

@Entity
@Immutable
@Table(name = "staff")
class CaseloadStaff(
    @Id
    @Column(name = "staff_id")
    private val id: Long,
    @Column(name = "officer_code", columnDefinition = "char(7)")
    private val code: String,
)
