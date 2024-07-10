package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Manager
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.ProbationArea
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Staff
import uk.gov.justice.digital.hmpps.integrations.delius.provider.entity.Team
import uk.gov.justice.digital.hmpps.integrations.delius.reference.entity.ReferenceData
import java.util.stream.Stream

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val nomsId: String?,

    @ManyToOne
    @JoinColumn(name = "current_tier")
    val currentTier: ReferenceData? = null,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @OneToMany(mappedBy = "person")
    @SQLRestriction("active_flag = 1")
    val managers: List<PersonManager> = listOf(),

    @Id
    @Column(name = "offender_id")
    val id: Long
) {
    val manager: PersonManager
        get() = managers.first()
}

@Immutable
@Entity
@SQLRestriction("soft_deleted = 0")
@Table(name = "offender_manager")
class PersonManager(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "team_id")
    override val team: Team,

    @ManyToOne
    @JoinColumn(name = "allocation_staff_id")
    override val staff: Staff,

    @ManyToOne
    @JoinColumn(name = "probation_area_id")
    override val probationArea: ProbationArea,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean = true,

    @Column(name = "soft_deleted", columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
) : Manager

interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = ["currentTier", "managers.team.district", "managers.staff.user", "managers.probationArea"])
    fun findByNomsId(nomsId: String): Person?

    @EntityGraph(attributePaths = ["currentTier", "managers.team.district", "managers.staff.user", "managers.probationArea"])
    fun findByCrn(crn: String): Person?

    @Query("select p.id from Person p where p.nomsId = :nomsId and p.softDeleted = false")
    fun findIdFromNomsId(nomsId: String): Long?

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select p.id from Person p where p.id = :personId")
    fun findForUpdate(personId: Long): Long

    @Query(
        """
            select p.noms_number 
            from offender p
            join event e on e.offender_id = p.offender_id and e.active_flag = 1 and e.soft_deleted = 0
            join disposal d on d.event_id = e.event_id and d.active_flag = 1 and d.soft_deleted = 0
            join custody c on c.disposal_id = d.disposal_id and c.soft_deleted = 0
            where p.crn = :crn
            and p.noms_number is not null and p.soft_deleted = 0
            group by p.noms_number
            having count(p.noms_number) = 1
    """, nativeQuery = true
    )
    fun findNomsSingleCustodial(crn: String): String?

    @Query(
        """
            select p.noms_number 
            from offender p
            join event e on e.offender_id = p.offender_id and e.active_flag = 1 and e.soft_deleted = 0
            join disposal d on d.event_id = e.event_id and d.active_flag = 1 and d.soft_deleted = 0
            join custody c on c.disposal_id = d.disposal_id and c.prisoner_number is not null and c.soft_deleted = 0
            join r_standard_reference_list cs on cs.standard_reference_list_id = c.custodial_status_id and cs.code_value <> 'P'
            where p.noms_number is not null and p.soft_deleted = 0
            group by p.noms_number
            having count(p.noms_number) = 1
    """, nativeQuery = true
    )
    fun findNomsSingleCustodial(): Stream<String>
}

fun PersonRepository.getByNomsId(nomsId: String) =
    findByNomsId(nomsId) ?: throw NotFoundException("Person", "nomsId", nomsId)

fun PersonRepository.getByCrn(crn: String) =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
