package uk.gov.justice.digital.hmpps.integration.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integration.delius.person.entity.Person

@Immutable
@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Disposal(

    @OneToOne
    @JoinColumn(name = "event_id")
    val event: Event,

    @OneToMany(mappedBy = "disposal")
    val requirements: MutableList<Requirement> = mutableListOf(),

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "disposal_id")
    val id: Long
)

@Immutable
@Entity
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class Event(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @OneToOne(mappedBy = "event")
    val disposal: Disposal?,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "event_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class Requirement(
    @Id
    @Column(name = "rqmnt_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory?,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(updatable = false, columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
class RequirementMainCategory(
    @Id
    @Column(name = "rqmnt_type_main_category_id")
    val id: Long,

    val code: String,
    val description: String,

    @Column(name = "restrictive", columnDefinition = "char(1)")
    @Convert(converter = NumericBooleanConverter::class)
    val restrictive: Boolean,

) {
    companion object {
        const val UPW_RQMNT_MAIN_CATEGORY = "W"
    }
}

interface EventRepository : JpaRepository<Event, Long> {
    fun findByPersonId(personId: Long): List<Event>
}
