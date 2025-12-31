package uk.gov.justice.digital.hmpps.appointments.domain.event.component

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.appointments.domain.event.Disposal
import uk.gov.justice.digital.hmpps.appointments.domain.person.Person

@Immutable
@Entity
@Table(name = "rqmnt")
@SQLRestriction("soft_deleted = 0")
open class Requirement(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "rqmnt_type_main_category_id")
    val mainCategory: RequirementMainCategory,

    @ManyToOne
    @JoinColumn(name = "disposal_id")
    val disposal: Disposal,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "rqmnt_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_rqmnt_type_main_category")
open class RequirementMainCategory(
    val code: String,
    @Id
    @Column(name = "rqmnt_type_main_category_id")
    val id: Long
) {
    enum class Code(val value: String) {
        REHAB_ACTIVITY_TYPE("F")
    }
}

interface RequirementRepository : JpaRepository<Requirement, Long>