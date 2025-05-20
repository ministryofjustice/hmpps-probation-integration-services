package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "offender_manager")
class ManagerHistory(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: ManagerHistoryPerson,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val probationArea: ManagerHistoryProbationArea,

    @Column(name = "allocation_date")
    val allocationDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,
)

@Entity
@Immutable
@Table(name = "offender")
class ManagerHistoryPerson(
    @Id
    @Column(name = "offender_id")
    val id: Long,
    @Column(columnDefinition = "char(7)")
    val crn: String,
)

@Entity
@Immutable
@Table(name = "probation_area")
class ManagerHistoryProbationArea(
    @Id
    @Column(name = "probation_area_id")
    val id: Long,
    @Column(columnDefinition = "char(3)")
    val code: String,
    val description: String,
)

interface ManagerHistoryRepository : JpaRepository<ManagerHistory, Long> {
    fun findByPersonCrnInOrderByPersonCrn(crns: List<String>): List<ManagerHistory>
}
