package uk.gov.justice.digital.hmpps.integrations.delius.reference.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
)
