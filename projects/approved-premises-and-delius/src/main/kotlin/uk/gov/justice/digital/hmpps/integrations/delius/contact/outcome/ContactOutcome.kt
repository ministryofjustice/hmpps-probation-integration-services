package uk.gov.justice.digital.hmpps.integrations.delius.contact.outcome

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,
    val code: String,
) {
    companion object {
        val AP_DEPARTED_PREFIX = "AP_"
        val AP_NON_ARRIVAL_PREFIX = "AP-"
    }
}

interface ContactOutcomeRepository : JpaRepository<ContactOutcome, Long> {
    fun findByCode(code: String): ContactOutcome?
}
