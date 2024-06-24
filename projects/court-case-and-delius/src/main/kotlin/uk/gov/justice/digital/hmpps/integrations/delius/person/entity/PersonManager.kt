package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationAreaEntity
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.io.Serializable
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team?,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff?,

    @Column(name = "staff_employee_id")
    val staffEmployeeId: Long,

    @Column(name = "trust_provider_flag", columnDefinition = "number")
    val trustProviderFlag: Boolean,

    @Column(name = "officer_code", columnDefinition = "char(7)")
    val officerCode: String,

    @ManyToOne
    @JoinColumns(
        JoinColumn(
            name = "staff_employee_id",
            referencedColumnName = "staff_employee_id",
            insertable = false,
            updatable = false
        ),
        JoinColumn(
            name = "trust_provider_flag",
            referencedColumnName = "trust_provider_flag",
            insertable = false,
            updatable = false
        )
    )
    val officer: Officer,

    @OneToMany
    @JoinColumns(
        JoinColumn(
            name = "offender_manager_id",
            referencedColumnName = "offender_manager_id",
            insertable = false,
            updatable = false
        ), JoinColumn(name = "offender_id", referencedColumnName = "offender_id", insertable = false, updatable = false)
    )
    val responsibleOfficers: List<ResponsibleOfficer> = emptyList(),

    @ManyToOne
    @JoinColumn(name = "provider_employee_id")
    val providerEmployee: ProviderEmployee? = null,

    @ManyToOne
    @JoinColumn(name = "partition_area_id")
    val partitionArea: PartitionArea,

    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val provider: ProbationAreaEntity,

    @ManyToOne
    @JoinColumn(name = "allocation_reason_id")
    val allocationReason: ReferenceData,

    @Column(name = "allocation_date")
    val date: ZonedDateTime,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    var softDeleted: Boolean = false

) {
    @Transient
    var emailAddress: String? = null

    @Transient
    var telephoneNumber: String? = null

    fun responsibleOfficer(): ResponsibleOfficer? = responsibleOfficers.firstOrNull { it.isActive() }

    fun isUnallocated(): Boolean = staff?.code?.endsWith("U") ?: false
}

@Immutable
@Entity
@Table(name = "officer")
class Officer(

    @EmbeddedId
    val id: OfficerPk,

    @Column(name = "surname")
    val surname: String,

    @Column(name = "forename")
    val forename: String,

    @Column(name = "forename2")
    val forename2: String? = null,
)

@Immutable
@Entity
@Table(name = "provider_employee")
class ProviderEmployee(
    @Id
    @Column(name = "provider_employee_id")
    val providerEmployeeId: Long,

    @Column(name = "surname")
    val surname: String,

    @Column(name = "forename")
    val forename: String,

    @Column(name = "forename2")
    val forename2: String? = null
)

@Embeddable
class OfficerPk(
    @Column(name = "trust_provider_flag")
    val trustProviderFlag: Long,

    @Column(name = "staff_employee_id")
    val staffEmployeeId: Long
) : Serializable



