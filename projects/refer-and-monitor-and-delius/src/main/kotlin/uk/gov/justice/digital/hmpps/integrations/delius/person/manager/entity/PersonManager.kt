package uk.gov.justice.digital.hmpps.integrations.delius.person.manager.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.Where
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "offender_manager")
@Where(clause = "soft_deleted = 0 and active_flag = 1")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @OneToOne(mappedBy = "communityManager")
    val responsibleOfficer: ResponsibleOfficer?,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "responsible_officer")
@Where(clause = "end_date is null")
class ResponsibleOfficer(
    @OneToOne
    @JoinColumn(name = "offender_manager_id")
    val communityManager: PersonManager?,

    val endDate: ZonedDateTime? = null,

    @Id
    @Column(name = "responsible_officer_id")
    private val id: Long
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @EntityGraph(attributePaths = ["person", "responsibleOfficer"])
    fun findByPersonCrn(crn: String): PersonManager?
}
