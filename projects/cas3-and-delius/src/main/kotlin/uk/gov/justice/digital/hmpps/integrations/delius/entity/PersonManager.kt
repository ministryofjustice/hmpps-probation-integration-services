package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "team_id")
    override val teamId: Long,

    @Column(name = "allocation_staff_id")
    override val staffId: Long,

    @Column(name = "probation_area_id")
    override val probationAreaId: Long,

    @Column(name = "active_flag", columnDefinition = "NUMBER")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false

) : ManagerIds

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @Query(
        """
        select pm from PersonManager pm 
        where pm.personId = :personId
        and pm.active = true
        and pm.softDeleted = false
        """
    )
    fun findActiveManager(personId: Long): ManagerIds?
}

fun PersonManagerRepository.getActiveManager(personId: Long) =
    findActiveManager(personId) ?: throw NotFoundException("Manager", "personId", personId)

interface ManagerIds {
    val probationAreaId: Long
    val teamId: Long
    val staffId: Long
}
