package uk.gov.justice.digital.hmpps.epf.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
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

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

interface ResponsibleOfficerRepository : JpaRepository<ResponsibleOfficer, Long> {

    @EntityGraph(attributePaths = ["communityManager.provider", "prisonManager.provider"])
    fun findByPersonIdAndEndDateIsNull(personId: Long): ResponsibleOfficer?
}

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @EntityGraph(attributePaths = ["provider"])
    fun findByPersonId(personId: Long): PersonManager?
}
