package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
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
    @JoinColumn(name = "team_id")
    val team: ManagerHistoryTeam,

    @Column(name = "allocation_date")
    val allocationDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate? = null,
) {
    @Column(name = "probation_area_id")
    val probationAreaId: Long = team.probationAreaId
}

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
@Table(name = "team")
class ManagerHistoryTeam(
    @Column(name = "code", columnDefinition = "char(6)")
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "district_id")
    val district: District,

    @Column(name = "probation_area_id")
    val probationAreaId: Long,

    @Id
    @Column(name = "team_id")
    val id: Long,
)

interface ManagerHistoryRepository : JpaRepository<ManagerHistory, Long> {
    @EntityGraph(attributePaths = ["team.district.borough.probationArea"])
    fun findByPersonCrnInOrderByPersonCrn(crns: List<String>): List<ManagerHistory>
}
