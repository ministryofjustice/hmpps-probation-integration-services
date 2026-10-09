package uk.gov.justice.digital.hmpps.integrations.delius.caseload

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.Team

@Entity
@Immutable
@SQLRestriction("role_code = 'OM'")
data class Caseload(
    @Id
    @Column(name = "caseload_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "staff_employee_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "trust_provider_team_id")
    val team: Team,

    @Column(name = "role_code")
    val roleCode: String,

    @Column(name = "trust_provider_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean = false,

    @Column(name = "event_id")
    val eventId: Long? = null,
)

interface CaseloadRepository : JpaRepository<Caseload, Long> {
    @Query(
        """
        select c from Caseload c 
        where c.staff.code = :staffCode
        and c.roleCode = 'OM'
        and  c.trustProviderFlag = false
    """
    )
    fun findAllByStaffCode(staffCode: String): List<Caseload>

    @Query(
        """
        select c from Caseload c
        where c.staff.code in :staffCodes
        and c.roleCode = 'OM'
        and c.trustProviderFlag = false
    """
    )
    fun findAllByStaffCodeIn(staffCodes: List<String>): List<Caseload>
}