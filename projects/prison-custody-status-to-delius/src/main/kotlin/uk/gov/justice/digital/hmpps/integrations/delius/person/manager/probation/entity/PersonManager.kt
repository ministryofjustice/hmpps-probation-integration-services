package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.probation.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.probationarea.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.staff.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.team.entity.Team

@Entity
@Immutable
@Table(name = "offender_manager")
class PersonManager(
    @Id
    @Column(name = "offender_manager_id", nullable = false)
    val id: Long,
    @Column(name = "offender_id", nullable = false)
    val personId: Long,
    @ManyToOne
    @JoinColumn(name = "allocation_reason_id", nullable = false)
    val allocationReason: ReferenceData,
    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    val team: Team,
    @ManyToOne
    @JoinColumn(name = "probation_area_id", nullable = false)
    val probationArea: ProbationArea,
    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,
    @Column(columnDefinition = "number", nullable = false)
    val softDeleted: Boolean = false,
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    fun findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(personId: Long): PersonManager?
}

fun PersonManagerRepository.getByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(personId: Long): PersonManager =
    findByPersonIdAndActiveIsTrueAndSoftDeletedIsFalse(personId) ?: throw NotFoundException(
        "PersonManager",
        "personId",
        personId,
    )
