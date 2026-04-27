package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "offender")
class Person(

    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @OneToOne(mappedBy = "person")
    val manager: PersonManager,

    @OneToMany(mappedBy = "person")
    @SQLRestriction("register_type_id in (select register_type_id from r_register_type where r_register_type.code in ('RLRH', 'RMRH', 'RHRH', 'RVHR'))")
    val roshRegistrations: List<Registration>,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val noms: String?,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String?,

    val exclusionMessage: String? = null,

    val restrictionMessage: String? = null,
)

interface PersonRepository : JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = ["gender", "manager.team", "manager.staff.user", "roshRegistrations.person", "roshRegistrations.type"])
    fun findByCrn(crn: String): Person?

    @EntityGraph(attributePaths = ["gender", "manager.team", "manager.staff.user", "roshRegistrations.person", "roshRegistrations.type"])
    fun findByManagerTeamIdIn(teamIds: List<Long>): List<Person>
}
