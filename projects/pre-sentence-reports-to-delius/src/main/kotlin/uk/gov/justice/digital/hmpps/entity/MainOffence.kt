package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Table(name = "main_offence")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @Column(name = "offence_date")
    val date: LocalDate,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @OneToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,
)

@Entity
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    val mainCategoryCode: String,
    val mainCategoryDescription: String,
    val subCategoryCode: String,
    val subCategoryDescription: String,
)

interface MainOffenceRepository : JpaRepository<MainOffence, Long> {
    fun findByPersonCrnAndEventEventNumber(crn: String, eventNumber: String): MainOffence?
}