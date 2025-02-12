package uk.gov.justice.digital.hmpps.integrations.delius.sentence.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import uk.gov.justice.digital.hmpps.integrations.delius.overview.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.Provider
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "prison_offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PrisonManager(
    @Id
    @Column(name = "prison_offender_manager_id", nullable = false)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @ManyToOne
    @JoinColumn(name = "allocation_team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id", nullable = false)
    val staff: Staff,

    val allocationDate: LocalDate,

    @Column(name = "last_updated_datetime")
    val lastUpdated: ZonedDateTime,

    val endDate: LocalDate? = null,

    @OneToOne(mappedBy = "prisonManager")
    val responsibleOfficer: ResponsibleOfficer? = null,

    @Column(name = "active_flag", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val active: Boolean,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

interface PrisonManagerRepository : CrudRepository<PrisonManager, Long> {
    @Query(
        """
        SELECT om
        FROM PrisonManager om
        WHERE om.person.id = :id
        """
    )
    fun findPrisonManagerByPersonId(id: Long): PrisonManager?
}
