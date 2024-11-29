package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Immutable
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @Column(name = "allocation_staff_id")
    val staffId: Long,

    @Column(name = "team_id")
    val teamId: Long,

    @Column(name = "probation_area_id")
    val providerId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    fun findByPersonId(id: Long): PersonManager?
}

fun PersonManagerRepository.getManager(personId: Long) = findByPersonId(personId)
    ?: throw NotFoundException("Manager", "personId", personId)