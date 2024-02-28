package uk.gov.justice.digital.hmpps.integrations.delius.overview

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import java.time.LocalDate

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(columnDefinition = "char(7)")
    val nomsNumber: String? = null,

    @Column
    val croNumber: String? = null,

    @Column(columnDefinition = "char(13)")
    val pncNumber: String? = null,

    @Column
    val mostRecentPrisonerNumber: String? = null,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "third_name", length = 35)
    val thirdName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "preferred_name", length = 35)
    val preferredName: String?,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @Column(name = "telephone_number")
    val telephoneNumber: String?,

    @Column(name = "mobile_number")
    val mobileNumber: String?,

    @Column(name = "e_mail_address")
    val emailAddress: String?,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @OneToMany(mappedBy = "personId")
    val personalCircumstances: List<PersonalCircumstance>,

    @OneToMany(mappedBy = "personId")
    val disabilities: List<Disability>,

    @OneToMany(mappedBy = "personId")
    val provisions: List<Provision>,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false

)

interface PersonOverviewRepository : JpaRepository<Person, Long> {
    @EntityGraph(attributePaths = ["gender"])
    fun findByCrn(crn: String): Person?
}

fun PersonOverviewRepository.getPerson(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)


