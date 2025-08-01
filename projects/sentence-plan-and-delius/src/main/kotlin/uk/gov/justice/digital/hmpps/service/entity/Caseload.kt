package uk.gov.justice.digital.hmpps.service.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
class Caseload(
    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "staff_employee_id")
    val staff: Staff,

    @Id
    @Column(name = "caseload_id")
    val id: Long,
)

interface CaseloadRepository : JpaRepository<Caseload, Long> {
    fun countByStaffIdAndPersonCrn(staffId: Long, crn: String): Int
}