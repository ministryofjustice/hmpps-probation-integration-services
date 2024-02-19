package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Table(name = "offender_manager")
@Entity
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    fun findByPersonCrn(crn: String): PersonManager?
}
