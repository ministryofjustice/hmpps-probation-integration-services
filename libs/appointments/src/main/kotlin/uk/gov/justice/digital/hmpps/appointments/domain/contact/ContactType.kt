package uk.gov.justice.digital.hmpps.appointments.domain.contact

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "r_contact_type")
open class ContactType(
    val code: String,
    val description: String,

    @Column(name = "national_standards_contact")
    @Convert(converter = YesNoConverter::class)
    val nationalStandards: Boolean,

    @Column(name = "attendance_contact")
    @Convert(converter = YesNoConverter::class)
    val attendance: Boolean,

    @Column(name = "sensitive_contact")
    @Convert(converter = YesNoConverter::class)
    val sensitive: Boolean,

    @Convert(converter = YesNoConverter::class)
    val rarActivity: Boolean?,

    @Id
    @Column(name = "contact_type_id")
    val id: Long,
) {
    enum class Code(val value: String) {
        REVIEW_ENFORCEMENT_STATUS("ARWS"),
    }
}

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
open class ContactOutcome(
    val code: String,
    val description: String,

    @Column(name = "outcome_attendance")
    @Convert(converter = YesNoConverter::class)
    val attended: Boolean?,

    @Column(name = "outcome_compliant_acceptable")
    @Convert(converter = YesNoConverter::class)
    val acceptable: Boolean?,

    @Convert(converter = YesNoConverter::class)
    val enforceable: Boolean?,

    @Convert(converter = YesNoConverter::class)
    val actionRequired: Boolean,

    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,
)

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getTypeByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)

interface ContactOutcomeRepository : JpaRepository<ContactOutcome, Long> {
    fun findByCode(code: String): ContactOutcome?
}

fun ContactOutcomeRepository.getOutcomeByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("ContactOutcome", "code", code)