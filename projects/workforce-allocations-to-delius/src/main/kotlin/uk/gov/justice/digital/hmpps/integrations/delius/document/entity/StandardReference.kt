package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "R_STANDARD_REFERENCE_LIST")
@Immutable
class StandardReference(
    @Id @Column(name = "standard_reference_list_id")
    var id: Long,

    @Column(name = "code_description")
    val description: String,
)
