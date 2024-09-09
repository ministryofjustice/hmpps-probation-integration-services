package uk.gov.justice.digital.hmpps.integration.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
data class ReferenceData(

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @ManyToOne
    @JoinColumn(name = "reference_data_master_id")
    val dataset: Dataset,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,

    @Column(name = "code_set_name")
    val code: String
)