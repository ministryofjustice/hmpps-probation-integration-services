package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity.ReferenceData

@Entity
@Immutable
@Table(name = "personal_contact")
class DocPersonalContact(
    @Id
    @Column(name = "personal_contact_id")
    var id: Long,
    @Column(name = "first_name")
    val forename: String,
    @Column(name = "surname")
    val surname: String,
    @ManyToOne
    @JoinColumn(name = "title_id", insertable = false, updatable = false)
    val title: ReferenceData?,
)
