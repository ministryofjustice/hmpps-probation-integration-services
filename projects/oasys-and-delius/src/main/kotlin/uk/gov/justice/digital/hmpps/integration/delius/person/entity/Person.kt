package uk.gov.justice.digital.hmpps.integration.delius.person.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integration.delius.reference.entity.ReferenceData
import java.time.LocalDate

@Entity
@Immutable
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class Person(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "offender")
@SQLRestriction("soft_deleted = 0")
class PersonDetail(

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "noms_number", columnDefinition = "char(7)")
    val noms: String?,

    @Column(name = "pnc_number", columnDefinition = "char(13)")
    val pnc: String?,

    @Column(name = "cro_number", columnDefinition = "char(12)")
    val cro: String?,

    val firstName: String,
    val secondName: String?,
    val thirdName: String?,
    val surname: String,

    @Column(name = "date_of_birth_date")
    val dob: LocalDate,

    @Column(name = "telephone_number")
    val telephoneNumber: String?,

    @Column(name = "mobile_number")
    val mobileNumber: String?,

    @Column(name = "e_mail_address")
    val emailAddress: String?,

    @ManyToOne
    @JoinColumn(name = "gender_id")
    val gender: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "ethnicity_id")
    val ethnicity: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "language_id")
    val language: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "religion_id")
    val religion: ReferenceData?,

    @Column(columnDefinition = "number")
    val softDeleted: Boolean,

    @Id
    @Column(name = "offender_id")
    val id: Long
)

interface PersonDetailRepository : JpaRepository<PersonDetail, Long> {
    @EntityGraph(attributePaths = ["gender", "ethnicity", "language", "religion"])
    fun findByCrn(crn: String): PersonDetail?
}

fun PersonDetailRepository.getPersonDetail(crn: String): PersonDetail =
    findByCrn(crn) ?: throw NotFoundException("Person", "crn", crn)