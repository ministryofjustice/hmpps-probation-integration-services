package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

interface CaseloadRepository : JpaRepository<Caseload, Long> {
    fun findByStaffCodeAndRoleCode(staffCode: String, role: String): List<Caseload>
    fun findByTeamCodeAndRoleCodeOrderByAllocationDateDesc(staffCode: String, role: String): List<Caseload>
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

    val allocationDate: LocalDate?,
    val roleCode: String,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    val firstName: String,
    val secondName: String?,
    val surname: String,
    val startDate: LocalDate?,

    @Id
    val caseloadId: Long
) {
    enum class CaseloadRole(val value: String) {
        OFFENDER_MANAGER("OM"),
        ORDER_SUPERVISOR("OS")
    }
}

