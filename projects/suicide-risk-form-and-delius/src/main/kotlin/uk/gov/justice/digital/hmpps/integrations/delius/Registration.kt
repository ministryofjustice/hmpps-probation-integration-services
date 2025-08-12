package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.annotations.SQLRestriction
import org.hibernate.type.NumericBooleanConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.model.CodedDescription
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Immutable
@Table(name = "registration")
@SQLRestriction("soft_deleted = 0")
class Registration(

    @Id
    @Column(name = "registration_id")
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "offender_id")
    val person: Person,

    @ManyToOne
    @JoinColumn(name = "register_type_id")
    val type: RegisterType,

    @Column(name = "registration_date")
    val date: LocalDate,

    @ManyToOne
    @JoinColumn(name = "register_level_id")
    val level: ReferenceData?,

    @Column(name = "registration_notes", columnDefinition = "clob")
    val notes: String?,

    @Column(name = "document_linked", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val documentLinked: Boolean,

    @Column(name = "deregistered", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val deregistered: Boolean,

    @OneToMany(mappedBy = "registration")
    val deregistrations: List<Deregistration> = emptyList(),

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean,

    @Column(name = "created_datetime")
    val createdDateTime: LocalDateTime,
)

@Entity
@Immutable
@Table(name = "r_register_type")
class RegisterType(
    @Column
    val code: String,

    @Column
    val description: String,

    @ManyToOne
    @JoinColumn(name = "register_type_flag_id")
    val flag: ReferenceData,

    @Id
    @Column(name = "register_type_id")
    val id: Long
)

@Immutable
@Entity
@Table(name = "deregistration")
class Deregistration(
    @Id
    @Column(name = "deregistration_id", nullable = false)
    val id: Long,

    @Column(name = "deregistration_date")
    val deRegistrationDate: LocalDate,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registration_id", nullable = false)
    val registration: Registration,

    @Column(name = "deregistering_notes", columnDefinition = "clob")
    val notes: String?,

    @Column(name = "created_datetime")
    val createdDateTime: LocalDateTime,

    @Column(name = "soft_deleted", columnDefinition = "number")
    @Convert(converter = NumericBooleanConverter::class)
    val softDeleted: Boolean
)

fun RegisterType.codedDescription() = CodedDescription(code, description)

interface RegistrationRepository : JpaRepository<Registration, Long> {
    @Query(
        "SELECT r FROM Registration r " +
            "WHERE r.person.crn = :crn " +
            "AND r.softDeleted = false " +
            "AND r.type.code in ('ALSH', 'ALT7') " +
            "ORDER BY r.date DESC"
    )
    fun findLatestRelevantRegistrationForCrn(crn: String): Registration?
}