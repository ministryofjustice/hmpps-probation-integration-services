package uk.gov.justice.digital.hmpps.integrations.delius.contact.type

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "r_contact_type")
class ContactType(
    @Id
    @Column(name = "contact_type_id", nullable = false)
    val id: Long,

    @Column(name = "CODE", nullable = false)
    val code: String,
)
