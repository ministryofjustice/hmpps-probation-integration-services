package uk.gov.justice.digital.hmpps.integrations.delius.overview.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.hibernate.type.YesNoConverter
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity.ReferenceData
import uk.gov.justice.digital.hmpps.integrations.delius.user.entity.User
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
@EntityListeners(AuditingEntityListener::class)
class Person(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String?,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val noms: String?,

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
    var telephoneNumber: String?,

    @Column(name = "mobile_number")
    var mobileNumber: String?,

    @Column(name = "e_mail_address")
    var emailAddress: String?,

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
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    val exclusionMessage: String? = null,
    val restrictionMessage: String? = null,

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "last_updated_user_id")
    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(
            name = "last_updated_user_id",
            referencedColumnName = "user_id",
            insertable = false,
            updatable = false
        )
    )
    val lastUpdatedUser: User? = null,

    )

interface PersonSummaryEntity {
    val id: Long
    val forename: String
    val secondName: String?
    val thirdName: String?
    val surname: String
    val crn: String
    val pnc: String?
    val noms: String?
    val dateOfBirth: LocalDate
}

interface PersonRepository : JpaRepository<Person, Long> {

    @EntityGraph(attributePaths = ["gender", "religion", "language", "sexualOrientation", "genderIdentity", "lastUpdatedUser"])
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
        p.noms_number as noms,
        p.date_of_birth_date as dateofbirth
        from offender p 
        where p.crn = :crn and p.soft_deleted = 0  
        """, nativeQuery = true
    )
    fun findSummary(crn: String): PersonSummaryEntity?

    @Query(
        """
            select p.crn as crn, com.active as userIsCom from OffenderManager com join com.person p
            where com.staff.user.username = :username and p.crn in :crns and com.active = true and com.softDeleted = false
        """
    )
    fun userIsCom(username: String, crns: Set<String>): List<ComConfirmation>
}

interface ComConfirmation {
    val crn: String
    val userIsCom: Boolean
}

fun PersonRepository.getPerson(crn: String) = findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)
fun PersonRepository.getSummary(crn: String): PersonSummaryEntity =
    findSummary(crn) ?: throw NotFoundException("Person", "crn", crn)

