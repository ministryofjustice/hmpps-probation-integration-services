package uk.gov.justice.digital.hmpps.integrations.delius.person.registration.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.integrations.delius.person.Person
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceData
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
@Entity
@Table
@SQLRestriction("soft_deleted = 0")
class Registration(

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @ManyToOne
    @JoinColumn(name = "register_category_id")
    val category: ReferenceData?,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val level: ReferenceData?,

    @Column(name = "registration_date")
    val date: LocalDate,

    val nextReviewDate: LocalDate?,

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @OneToMany(mappedBy = "registration")
    val deregistrations: List<Deregistration> = emptyList(),

    @Column(columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deregistered: Boolean,

    val lastUpdatedDatetime: ZonedDateTime,

    @Column(name = "registration_notes", columnDefinition = "clob")
    val notes: String?,

    @Id
    @Column(name = "registration_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "r_register_type")
class RegisterType(

    @Column
    val code: String,

    val description: String,

    @ManyToOne
    @JoinColumn(name = "register_type_flag_id")
    val flag: ReferenceData?,

    @Id
    @Column(name = "register_type_id")
    val id: Long
) {
    enum class Code(val value: String) {
        GANG_AFFILIATION("STRG"),
        SEX_OFFENCE("ARSO"),
        MAPPA("MAPP")
    }
}

@Entity
@Table(name = "deregistration")
@SQLRestriction("soft_deleted = 0")
class Deregistration(
    @Id
    @Column(name = "deregistration_id")
    val id: Long = 0,

    @Column(name = "deregistration_date")
    val endDate: LocalDate,

    @ManyToOne
    @JoinColumn(name = "registration_id")
    val registration: Registration,

    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean = false,
)

interface RegistrationRepository : JpaRepository<Registration, Long> {
    fun existsByPersonIdAndTypeCode(personId: Long, code: String): Boolean

    @EntityGraph(attributePaths = ["type", "category", "level"])
    fun findByPersonId(personId: Long): List<Registration>

    @Query(
        """
            select r from Registration r
            where r.type.code in :codes
            and r.person.crn = :crn
        """
    )
    fun findByRegistrationCodes(crn: String, codes: List<String>): List<Registration>
}

enum class Category(val number: Int) { X9(0), M1(1), M2(2), M3(3), M4(4) }
enum class Level(val number: Int) { M0(0), M1(1), M2(2), M3(3) }
