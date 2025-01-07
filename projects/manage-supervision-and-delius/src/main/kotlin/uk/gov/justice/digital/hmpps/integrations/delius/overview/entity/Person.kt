package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String?,

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

    @Column(name = "previous_surname")
    val previousSurname: String? = null,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "religion_id")
    val religion: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "language_id")
    val language: ReferenceData? = null,

    @ManyToOne
    @JoinColumn(name = "sexual_orientation_id")
    val sexualOrientation: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "gender_identity_id")
    val genderIdentity: ReferenceData?,

    val genderIdentityDescription: String?,

    @Column(name = "Interpreter_required")
    @Convert(converter = YesNoConverter::class)
    val requiresInterpreter: Boolean? = false,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean = false,

    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null,

    )

interface PersonSummaryEntity {
    val id: Long
    val forename: String
    val secondName: String?
    val thirdName: String?
    val surname: String
    val crn: String
    val pnc: String?
    val dateOfBirth: LocalDate
}

interface PersonRepository : JpaRepository<Person, Long> {

    fun findByCrn(crn: String): Person?

    @Query(
        """
        select 
        p.offender_id as id, 
        p.first_name as forename, 
        p.second_name as secondname, 
        p.third_name as thirdname, 
        p.surname, 
        p.crn, 
        p.pnc_number as pnc, 
        p.date_of_birth_date as dateofbirth
        from offender p 
        where p.crn = :crn and p.soft_deleted = 0  
        """, nativeQuery = true
    )
    fun findSummary(crn: String): PersonSummaryEntity?
}

fun PersonRepository.getPerson(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
fun PersonRepository.getSummary(crn: String): PersonSummaryEntity =
    findSummary(crn) ?: throw NotFoundException("Person", "crn", crn)

