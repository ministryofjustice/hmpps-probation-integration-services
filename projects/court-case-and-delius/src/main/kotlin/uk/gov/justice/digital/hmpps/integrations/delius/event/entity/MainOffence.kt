package uk.gov.justice.digital.hmpps.integrations.delius.event.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.entity.ReferenceData
import java.time.LocalDateTime
import java.time.ZonedDateTime

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

    @Column(name = "offence_date", nullable = false)
    val date: LocalDateTime,

    @Column(nullable = false)
    val offenceCount: Long,

    val tics: Long?,

    val verdict: String?,

    @Column(nullable = false)
    val offenderId: Long,

    @Column(name = "created_datetime", nullable = false)
    val created: ZonedDateTime,

    @Column(name = "last_updated_datetime", nullable = false)
    val updated: ZonedDateTime,

    @Column(updatable = false, columnDefinition = "NUMBER")
    @Convert(converter = NumericBooleanConverter::class)
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
    val date: LocalDateTime?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    val offenceCount: Long? = null,

    @Column(name = "created_datetime", nullable = false)
    val created: ZonedDateTime,

    @Column(name = "last_updated_datetime", nullable = false)
    val updated: ZonedDateTime,

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

    @Column(columnDefinition = "char(5)")
    val code: String,

    val description: String,

    val abbreviation: String? = null,

    @Column(columnDefinition = "char(3)")
    val mainCategoryCode: String,

    val mainCategoryDescription: String,

    val mainCategoryAbbreviation: String,

    @Column(columnDefinition = "char(2)")
    val subCategoryCode: String,

    val subCategoryDescription: String,

    @Column(name = "form_20_code")
    val form20Code: String? = null,

    val subCategoryAbbreviation: String? = null,

    val cjitCode: String? = null

)
