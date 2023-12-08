package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
data class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,
    @Column(name = "code_description")
    val description: String,
)
