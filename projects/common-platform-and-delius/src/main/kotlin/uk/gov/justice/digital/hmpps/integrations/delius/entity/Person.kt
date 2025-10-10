package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person.Companion.CRO
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person.Companion.DOB
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person.Companion.FORENAME
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person.Companion.PNC
import uk.gov.justice.digital.hmpps.integrations.delius.entity.Person.Companion.SURNAME
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
@SequenceGenerator(name = "offender_id_seq", sequenceName = "offender_id_seq", allocationSize = 1)
@EntityListeners(AuditingEntityListener::class)
class Person(
    @Id
    @Column(name = "offender_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "offender_id_seq")
    val id: Long? = null,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column
    val croNumber: String? = null,

    @Column(columnDefinition = "char(13)")
    val pncNumber: String? = null,

    @Column(name = "first_name", length = 35)
    val forename: String,

    @Column
    val surnameSoundex: String,

    @Column
    val firstNameSoundex: String,

    @Column
    val middleNameSoundex: String?,

    @Column(name = "second_name", length = 35)
    val secondName: String? = null,

    @Column(name = "surname", length = 35)
    val surname: String,

    @Column(name = "date_of_birth_date")
    val dateOfBirth: LocalDate,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData,

    @Column(name = "telephone_number")
    val telephoneNumber: String? = null,

    @Column(name = "mobile_number")
    val mobileNumber: String? = null,

    @Column(name = "e_mail_address")
    val email: String? = null,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,

    @Column(name = "current_disposal", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentDisposal: Boolean = false,

    @Column(name = "current_restriction", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val currentRestriction: Boolean = false,

    @Column(name = "pending_transfer", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val pendingTransfer: Boolean = false,

    @Column(name = "current_remand_status")
    var remandStatus: String? = null,

    @Column(name = "OFFENDER_DETAILS", columnDefinition = "clob")
    var notes: String? = null,

    @Column(name = "DEFENDANT_ID")
    var defendantId: String? = null,

    @Column
    @Version
    val rowVersion: Long = 0L,

    @CreatedDate
    var createdDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetime: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedDate
    var lastUpdatedDatetimeDiversit: ZonedDateTime = ZonedDateTime.now(),

    @LastModifiedBy
    var lastUpdatedUserIdDiversity: Long = 0,

    @CreatedBy
    var createdByUserId: Long = 0,

    @LastModifiedBy
    var lastUpdatedUserId: Long = 0,

    @Column
    val partitionAreaId: Long = 0L
) {
    companion object {
        val FORENAME = Person::forename.name
        val SURNAME = Person::surname.name
        val DOB = Person::dateOfBirth.name
        val PNC = Person::pncNumber.name
        val CRO = Person::croNumber.name
    }
}

interface PersonRepository : JpaRepository<Person, Long>, JpaSpecificationExecutor<Person> {
    @Query("select soundex(:name) from dual", nativeQuery = true)
    fun getSoundex(name: String): String

    @Query(value = "select offender_support_api.getnextcrn from dual", nativeQuery = true)
    fun getNextCrn(): String

    fun existsByDefendantId(defendantId: String): Boolean
}

fun matchesName(firstName: String, surname: String) = Specification<Person> { person, _, cb ->
    cb.and(
        cb.equal(cb.lower(person[SURNAME]), surname.lowercase()),
        cb.equal(cb.lower(person[FORENAME]), firstName.lowercase()),
    )
}

fun matchesDateOfBirth(dob: LocalDate) =
    Specification<Person> { person, _, cb -> cb.equal(person.get<LocalDate>(DOB), dob) }

fun matchesPnc(pnc: String) =
    Specification<Person> { person, _, cb -> cb.equal(cb.lower(person[PNC]), pnc.lowercase()) }

fun matchesCro(cro: String) =
    Specification<Person> { person, _, cb -> cb.equal(cb.lower(person[CRO]), cro.lowercase()) }