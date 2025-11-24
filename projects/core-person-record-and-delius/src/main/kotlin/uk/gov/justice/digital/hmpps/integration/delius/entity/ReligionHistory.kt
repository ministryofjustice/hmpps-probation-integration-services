package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender_religion_history")
class ReligionHistory(
    @Id
    @Column(name = "offender_religion_history_id")
    val id: Long = 0,

    @Column(name = "offender_id")
    val personId: Long = 0,

    @ManyToOne
    @JoinColumn(name = "religion_id")
    val referenceData: ReferenceData? = null,

    @Column(name = "start_date")
    val startDate: LocalDate,

    @Column(name = "end_date")
    val endDate: LocalDate? = null
)

interface religionHistoryRepository : JpaRepository<ReligionHistory, Long> {
    @EntityGraph(attributePaths = ["referenceData"])
    fun findAllByPersonId(
        personId: Long
    ): List<ReligionHistory>
}