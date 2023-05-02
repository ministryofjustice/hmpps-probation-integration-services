package uk.gov.justice.digital.hmpps.service

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.annotation.Immutable

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Column(name = "code_value")
    val code: String,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,

    @Column(name = "code_description")
    val description: String,
)