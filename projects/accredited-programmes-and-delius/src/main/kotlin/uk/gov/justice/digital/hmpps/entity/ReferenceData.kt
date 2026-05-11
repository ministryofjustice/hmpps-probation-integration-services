package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.model.CodedValue

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long,

    @Column(name = "code_value", length = 100, nullable = false)
    val code: String,

    @Column(name = "code_description", length = 500, nullable = false)
    val description: String,

    @ManyToOne
    @JoinColumn(name = "reference_data_master_id")
    val dataset: Dataset,
) {
    fun toCodedValue() = CodedValue(code, description)

    data class KnownValue(val code: String, val datasetCode: String)
    companion object {
        val REQUIREMENT_COMPLETED = KnownValue("51", "REQUIREMENT TERMINATION REASON")
        val LICENCE_CONDITION_COMPLETED = KnownValue("51", "LICENCE CONDITION TERMINATION REASON")
        val REJECTED_STATUS = KnownValue("TR", "TRANSFER STATUS")
        val REJECTED_DECISION = KnownValue("R", "ACCEPTED DECISION")
        val LICENCE_CONDITION_TRANSFER_REJECTION_REASON = KnownValue("TWR", "LICENCE AREA TRANSFER REJECTION REASON")
        val REQUIREMENT_TRANSFER_REJECTION_REASON = KnownValue("TE", "REQUIREMENT AREA TRANSFER REJECTION REASON")
    }
}

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
