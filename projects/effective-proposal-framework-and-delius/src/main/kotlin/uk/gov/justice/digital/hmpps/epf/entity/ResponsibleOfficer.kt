package uk.gov.justice.digital.hmpps.epf.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.ZonedDateTime

@Immutable
@Entity
@Table(name = "responsible_officer")
class ResponsibleOfficer(

    @Id
    @Column(name = "responsible_officer_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "offender_manager_id")
    val communityManager: PersonManager?,

    @ManyToOne
    @JoinColumn(name = "prison_offender_manager_id")
    val prisonManager: PrisonManager?,

    val endDate: ZonedDateTime? = null
)

@Immutable
@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long> {

    @EntityGraph(attributePaths = ["communityManager", "prisonManager"])
    fun findByPersonIdAndEndDateIsNull(personId: Long): ResponsibleOfficer?
}
