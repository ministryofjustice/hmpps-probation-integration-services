package uk.gov.justice.digital.hmpps.integrations.common.entity.contact.type

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id", nullable = false)
    val id: Long,
    @Column(nullable = false)
    val code: String,
)
