package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(
    @[Id Column(name = "offender_manager_id")]
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @Column(name = "allocation_staff_id")
    val staffId: Long,

    @Column
    val teamId: Long,

    @Column
    val probationAreaId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false

)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    fun findByPersonId(personId: Long): PersonManager?
}

fun PersonManagerRepository.getActiveManager(personId: Long) =
    findByPersonId(personId) ?: throw NotFoundException("Community manager", "person id", personId)
