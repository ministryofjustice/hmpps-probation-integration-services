package uk.gov.justice.digital.hmpps.integrations.delius.manager.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.entity.Person
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Provider
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender_manager")
@SQLRestriction("soft_deleted = 0 and active_flag = 1")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    val provider: Provider,

    @ManyToOne
    @JoinColumn(name = "team_id")
    val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    val staff: Staff,

    @Column
    val allocationDate: LocalDate,

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

interface PersonManagerRepository : JpaRepository<PersonManager, Long> {
    @Query("select pm from PersonManager pm where pm.person.crn = :crnOrNomisId or pm.person.nomsNumber = :crnOrNomisId")
    @EntityGraph(attributePaths = ["person", "provider", "team.district.borough", "team.addresses", "staff.user"])
    fun findByPersonCrnOrPersonNomsNumber(crnOrNomisId: String): PersonManager?

    @Query("select pm from PersonManager pm where pm.person.crn in :crnsOrNomisIds or pm.person.nomsNumber in :crnsOrNomisIds")
    @EntityGraph(attributePaths = ["person", "provider", "team.district.borough", "team.addresses", "staff.user"])
    fun findByPersonCrnInOrPersonNomsNumberIn(crnsOrNomisIds: List<String>): List<PersonManager>
}

