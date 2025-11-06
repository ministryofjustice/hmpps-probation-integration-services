package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.io.Serializable

@Entity
@Immutable
@Table(name = "r_contact_type")
class ContactType(
    val code: String,
    @Id
    @Column(name = "contact_type_id")
    val id: Long,
) {
    enum class Code(val value: String) {
        UNPAID_WORK_APPOINTMENT("CUPA")
    }
}

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String,

    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,
)

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
}