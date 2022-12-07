package uk.gov.justice.digital.hmpps.integrations.delius.document.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Table(name = "r_standard_reference_list")
@Immutable
class StandardReference(
    @Id @Column(name = "standard_reference_list_id")
    var id: Long,

    @Column(name = "code_description")
    val description: String,
)
