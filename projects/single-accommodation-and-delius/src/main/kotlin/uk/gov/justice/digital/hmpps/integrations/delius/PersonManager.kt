package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(

    @Id
    @Column(name = "offender_manager_id")
    val id: Long,

    @Column(name = "offender_id")
    val personId: Long,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column(name = "probation_area_id")
    val probationAreaId: Long,

    @Column(name = "active_flag", columnDefinition = "NUMBER")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "NUMBER", nullable = false)
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false
)

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    fun findByStaffId(staffId: Long): List<PersonManager>
    fun findByTeamIdIn(teamIds: List<Long>): List<PersonManager>
    fun findFirstByPersonId(personId: Long): PersonManager?
}
