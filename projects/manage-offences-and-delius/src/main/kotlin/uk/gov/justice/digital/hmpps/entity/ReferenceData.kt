package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
data class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,
    @Column(name = "code_value")
    val code: String,
    @Column(name = "code_description")
    val description: String,
    @ManyToOne
    @JoinColumn(name = "reference_data_master_id")
    val set: ReferenceDataSet,
)

@Entity
@Immutable
@Table(name = "r_reference_data_master")
class ReferenceDataSet(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,
    @Column(name = "code_set_name")
    val name: String,
)
