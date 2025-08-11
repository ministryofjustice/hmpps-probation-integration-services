package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter

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

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class Dataset(

    @Column(name = "code_set_name")
    val code: String,

    @Id
    @Column(name = "reference_data_master_id")
    val id: Long
) {
    companion object {
        const val ADDRESS_STATUS = "ADDRESS STATUS"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Dataset

        return code == other.code
    }

    override fun hashCode(): Int {
        return code.hashCode()
    }
}