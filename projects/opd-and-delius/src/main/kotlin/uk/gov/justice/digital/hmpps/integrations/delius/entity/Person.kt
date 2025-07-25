package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException
import uk.gov.justice.digital.hmpps.exception.IgnorableMessageException.Companion.orIgnore
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

@Immutable
@Table(name = "offender_manager")
@Entity
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "probation_area_id")
    val providerId: Long,

    val teamId: Long,

    @Column(name = "allocation_staff_id")
    val staffId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    fun findByPersonCrn(crn: String): PersonManager?
    fun findByPersonNomsId(nomsId: String): PersonManager?
}

fun PersonManagerRepository.getByCrnOrNoms(crn: String?, nomsId: String?) = when {
    crn != null -> findByPersonCrn(crn.uppercase()).orIgnore { "PersonNotFound" }
    nomsId != null -> findByPersonNomsId(nomsId).orIgnore { "PersonNotFound" }
    else -> throw IllegalArgumentException("No CRN or NOMS number provided")
}