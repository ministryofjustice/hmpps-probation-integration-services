package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "main_offence")
@SQLRestriction("soft_deleted = 0")
class MainOffence(
    @Id
    @Column(name = "main_offence_id")
    val id: Long,

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @ManyToOne
    @JoinColumn(name = "offence_id")
    val offence: Offence,

    @Column(name = "offence_date")
    val date: LocalDate,

    @Column(updatable = false, columnDefinition = "NUMBER")
    val softDeleted: Boolean = false
)

interface MainOffenceRepository : JpaRepository<MainOffence, Long> {
    fun findByEvent(event: Event): MainOffence
}

@Immutable
@Table(name = "additional_offence")
@Entity
@SQLRestriction("soft_deleted = 0")
class AdditionalOffence(

    @ManyToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @JoinColumn(name = "offence_id")
    @ManyToOne
    val offence: Offence,

    @Column(name = "offence_date")
    val date: LocalDate?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "additional_offence_id")
    val id: Long
)

interface AdditionalOffenceRepository : JpaRepository<AdditionalOffence, Long> {
    fun findByEvent(event: Event): List<AdditionalOffence>
}

@Immutable
@Entity
@Table(name = "r_offence")
class Offence(
    @Id
    @Column(name = "offence_id")
    val id: Long,

    @JoinColumn(name = "ogrs_offence_category_id", nullable = false)
    @ManyToOne
    val ogrsOffenceCategory: ReferenceData,

    @Column(nullable = false)
    val code: String,

    @Column(nullable = false)
    val description: String,

    val abbreviation: String?,

    @Column(nullable = false)
    val mainCategoryCode: String,

    @Column(nullable = false)
    val mainCategoryDescription: String,

    @Column(nullable = false)
    val mainCategoryAbbreviation: String,

    @Column(nullable = false)
    val subCategoryCode: String,

    @Column(nullable = false)
    val subCategoryDescription: String,

    val form20Code: String?,

    val subCategoryAbbreviation: String?,

    val cjitCode: String?

)
