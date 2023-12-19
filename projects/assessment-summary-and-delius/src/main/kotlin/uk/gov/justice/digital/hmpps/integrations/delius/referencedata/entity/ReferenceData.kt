package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity

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

    val description: String,

    @Column(name = "reference_data_master_id")
    val datasetId: Long,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
) {
    enum class Code(val value: String) {
        OASYS_RISK_FLAG("1")
    }
}

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(

    @Column(name = "code_set_name")
    val name: String,

    @Id
    @Column(name = "reference_data_master_id")
    val id: Long
) {
    enum class Code(val value: String) {
        REGISTER_TYPE_FLAG("REGISTER TYPE FLAG")
    }
}
