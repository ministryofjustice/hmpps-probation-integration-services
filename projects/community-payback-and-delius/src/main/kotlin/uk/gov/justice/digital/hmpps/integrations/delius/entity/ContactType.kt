package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.model.CodeDescription
import java.io.Serializable

@Entity
@Immutable
@Table(name = "r_contact_type")
class ContactType(
    val code: String,
    @Id
    @Column(name = "contact_type_id")
    val id: Long,

    @Column(name = "national_standards_contact")
    @Convert(converter = YesNoConverter::class)
    val nationalStandards: Boolean
) {
    enum class Code(val value: String) {
        UNPAID_WORK_APPOINTMENT("CUPA"),
        REVIEW_ENFORCEMENT_STATUS("ARWS")
    }
}

interface ContactTypeRepository : JpaRepository<ContactType, Long> {
    fun findByCode(code: String): ContactType?
}

fun ContactTypeRepository.getByCode(code: String) =
    findByCode(code) ?: throw NotFoundException("ContactType", "code", code)

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String,

    @Column(name = "outcome_attendance")
    @Convert(converter = YesNoConverter::class)
    val attended: Boolean? = null,

    @Column(name = "outcome_compliant_acceptable")
    @Convert(converter = YesNoConverter::class)
    val complied: Boolean? = null,

    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,
)

fun ContactOutcome.toCodeDescription() = CodeDescription(this.code, this.description)

@Immutable
@Entity
@Table(name = "r_contact_type_outcome")
class ContactTypeOutcome(
    @ManyToOne
    @JoinColumn(name = "contact_type_id", insertable = false, updatable = false)
    val type: ContactType,

    @ManyToOne
    @JoinColumn(name = "contact_outcome_type_id", insertable = false, updatable = false)
    val outcome: ContactOutcome,
) {
    @EmbeddedId
    val id: ContactTypeOutcomeId = ContactTypeOutcomeId(type.id, outcome.id)
}

@Embeddable
data class ContactTypeOutcomeId(
    @Column(name = "contact_type_id")
    val contactTypeId: Long,

    @Column(name = "contact_outcome_type_id")
    val contactOutcomeTypeId: Long
) : Serializable

interface ContactOutcomeRepository : JpaRepository<ContactOutcome, Long> {
    @Query(
        """
        select cto.outcome 
        from ContactTypeOutcome cto
        where cto.type.code = :typeCode
        """
    )
    fun findForTypeCode(typeCode: String): List<ContactOutcome>

    fun findContactOutcomeByCode(code: String): ContactOutcome?
}

fun ContactOutcomeRepository.getContactOutcome(code: String) =
    findContactOutcomeByCode(code) ?: throw NotFoundException("Contact Outcome", "code", code)
