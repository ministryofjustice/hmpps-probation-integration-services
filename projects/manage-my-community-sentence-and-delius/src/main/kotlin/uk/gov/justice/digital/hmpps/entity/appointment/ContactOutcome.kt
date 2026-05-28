package uk.gov.justice.digital.hmpps.entity.appointment

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,

    val code: String,

    val description: String
)