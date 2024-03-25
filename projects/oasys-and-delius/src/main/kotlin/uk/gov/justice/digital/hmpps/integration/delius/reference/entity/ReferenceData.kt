package uk.gov.justice.digital.hmpps.integration.delius.reference.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,
)
