package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Column(name = "code_value", length = 100, nullable = false)
    val code: String,
    @Column(name = "code_description")
    val description: String,
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,
)
