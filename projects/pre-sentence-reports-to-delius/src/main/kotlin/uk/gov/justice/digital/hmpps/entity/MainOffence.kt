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
    val offence: OffenceEntity,
)

@Entity
@Table(name = "r_offence")
class OffenceEntity(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @Column(name = "main_category_code", columnDefinition = "char(3)")
    val mainCategoryCode: String,
    val mainCategoryDescription: String,
    @Column(name = "sub_category_code", columnDefinition = "char(2)")
    val subCategoryCode: String,
    val subCategoryDescription: String,
)

interface MainOffenceRepository : JpaRepository<MainOffence, Long> {
    fun findByPersonCrnAndEventEventNumber(crn: String, eventNumber: String): MainOffence?
}