package uk.gov.justice.digital.hmpps.integrations.delius.caseload.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.LocalDate

interface CaseloadRepository : JpaRepository<Caseload, Long> {
    fun findByStaffIdAndRoleCode(id: Long, role: String): List<Caseload>
    fun findByStaffCodeAndRoleCode(staffCode: String, role: String): List<Caseload>
    fun findByTeamCodeAndRoleCodeOrderByAllocationDateDesc(teamCode: String, role: String): List<Caseload>

    @Query(
        """
        select c from Caseload c
        join fetch c.team t
        join fetch c.staff s
        left join fetch s.user
        where t.id in (:teamIds) and c.trustProviderFlag in (true, false)
        and c.roleCode = :role
        and (:query is null or trim(:query) = '' 
            or (c.crn is not null and lower(c.crn) like '%' || lower(:query) || '%')
            or (c.firstName is not null and lower(c.firstName) like '%' || lower(:query) || '%')
            or (c.secondName is not null and lower(c.secondName) like '%' || lower(:query) || '%')
            or (c.surname is not null and lower(c.surname) like '%' || lower(:query) || '%')
            or (c.firstName is not null and c.surname is not null and lower(c.firstName || ' ' || c.surname) like '%' || lower(:query) || '%')
            or (s.forename is not null and lower(s.forename) like '%' || lower(:query) || '%')
            or (s.surname is not null and lower(s.surname) like '%' || lower(:query) || '%')
            or (s.forename is not null and s.surname is not null and lower(s.forename || ' ' || s.surname) like '%' || lower(:query) || '%')
        )
    """
    )
    fun findByTeamIdInAndRoleCode(teamIds: List<Long>, role: String, query: String?, page: Pageable): Page<Caseload>
}

@Immutable
@EntityListeners(AuditingEntityListener::class)
@Entity
@Table(name = "caseload")
class Caseload(

    @ManyToOne
    @JoinColumn(name = "staff_employee_id")
    val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "trust_provider_team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    val allocationDate: LocalDate?,
    val roleCode: String,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    val firstName: String,
    val secondName: String?,
    val surname: String,
    val startDate: LocalDate?,

    @Column(name = "trust_provider_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val trustProviderFlag: Boolean,

    @Id
    val caseloadId: Long
) {
    enum class CaseloadRole(val value: String) {
        OFFENDER_MANAGER("OM"),
        ORDER_SUPERVISOR("OS")
    }
}

