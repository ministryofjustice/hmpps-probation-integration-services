package uk.gov.justice.digital.hmpps.integrations.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    @Id
    @Column(name = "offender_id")
    val id: Long
) {
    @Column(name = "current_highest_risk_colour")
    var highestRiskColour: String? = null

    @OneToOne(mappedBy = "person")
    @SQLRestriction("active_flag = 1 and soft_deleted = 0")
    var manager: PersonManager? = null
        private set
}

@Immutable
@Entity
@Table(name = "offender_manager")
@SQLRestriction("active_flag = 1 and soft_deleted = 0")
class PersonManager(

    @OneToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    val probationAreaId: Long,
    val teamId: Long,
    @Column(name = "allocation_staff_id")
    val staffId: Long,

    @Column(name = "active_flag", columnDefinition = "number")
    val active: Boolean,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_manager_id")
    val id: Long
)

interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = ["manager"])
    fun findByCrn(crn: String): Person?

    @Query(
        """
        select count(r) 
        from Requirement r
        left join r.mainCategory mc
        left join r.subCategory sc
        left join r.additionalMainCategory amc
        where r.person.id = :personId
        and (mc.code = 'RM38' 
            or (mc.code = '7' and (sc is null or sc.code <> 'RS66'))
            or (amc.code in ('RM38', '7')))
        and r.active = true and r.softDeleted = false
    """
    )
    fun countAccreditedProgrammeRequirements(personId: Long): Int

    @Modifying
    @Query(
        """
        merge into iaps_offender using dual on (offender_id = ?1) 
        when matched then update set iaps_flag=?2 
        when not matched then insert(offender_id, iaps_flag) values(?1,?2)
        """,
        nativeQuery = true
    )
    fun updateIaps(personId: Long, iapsFlagValue: Long = 1)
}

fun PersonRepository.getByCrn(crn: String) =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
