package uk.gov.justice.digital.hmpps.integrations.delius.person.offence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity.Event
import java.time.LocalDate

interface CaseOffence {
    val id: Long
    val code: String
    val description: String
    val mainCategoryDescription: String
    val subCategoryDescription: String
    val date: LocalDate?
    val main: Boolean
    val eventNumber: String
    val eventId: Long
}

@Immutable
@Table(name = "main_offence")
@Entity
@SQLRestriction("soft_deleted = 0")
class MainOffence(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @JoinColumn(name = "offence_id")
    @ManyToOne
    val offence: Offence,

    @Column(name = "offence_date")
    val date: LocalDate,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "main_offence_id")
    val id: Long
)

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
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "additional_offence_id")
    val id: Long
)

@Immutable
@Table(name = "r_offence")
@Entity
class Offence(

    @Column(columnDefinition = "char(5)")
    val code: String,

    @Column
    val description: String,

    @Column(name = "main_category_description")
    private var mainCategoryDescription: String,

    @Column(name = "sub_category_description")
    private val subCategoryDescription: String,

    @Id
    @Column(name = "offence_id")
    val id: Long
)

interface MainOffenceRepository : JpaRepository<MainOffence, Long> {
    @Query(
        """
        select 
            mo.id as id,
            mo.offence.code as code, 
            mo.offence.description as description, 
            mo.offence.mainCategoryDescription as mainCategoryDescription, 
            mo.offence.subCategoryDescription as subCategoryDescription, 
            mo.date as date, 
            true as main, 
            mo.event.number as eventNumber,
            mo.event.id as eventId
        from MainOffence mo
        where mo.event.person.id = :personId and mo.event.active = true
        union all
        select 
            ao.id,
            ao.offence.code, 
            ao.offence.description, 
            ao.offence.mainCategoryDescription as mainCategoryDescription, 
            ao.offence.subCategoryDescription as subCategoryDescription, 
            ao.date, 
            false, 
            ao.event.number,
            ao.event.id
        from AdditionalOffence ao
        where ao.event.person.id = :personId and ao.event.active = true
    """
    )
    fun findOffencesFor(personId: Long): List<CaseOffence>
}
