package uk.gov.justice.digital.hmpps.integrations.delius

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.model.CodedDescription
import java.io.Serializable

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Column(name = "code_value")
    override val code: String,

    @Column(name = "code_description")
    override val description: String,

    @ManyToOne
    @JoinColumn(name = "reference_data_master_id")
    val dataset: Dataset,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
) : CodeAndDescription {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReferenceData

        if (code != other.code) return false
        if (dataset != other.dataset) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + dataset.hashCode()
        return result
    }
}

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
        const val BREACH_CONDITION_TYPE = "BREACH CONDITION TYPE"
        const val BREACH_NOTICE_TYPE = "BREACH NOTICE TYPE"
        const val BREACH_REASON = "BREACH REASON"
        const val BREACH_SENTENCE_TYPE = "BREACH SENTENCE TYPE"
        const val REQUIREMENT_SUB_CATEGORY = "REQUIREMENT SUB CATEGORY"
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

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @EntityGraph(attributePaths = ["dataset"])
    fun findByDatasetCodeAndSelectableTrue(datasetCode: String): List<ReferenceData>
}

@Embeddable
data class LinkedListId(
    @Column(name = "standard_reference_data1")
    val data1: Long = 0,

    @Column(name = "standard_reference_data2")
    val data2: Long = 0,
) : Serializable

@Immutable
@Entity
@Table(name = "r_linked_list")
class LinkedList(
    @EmbeddedId
    val id: LinkedListId,

    @ManyToOne
    @JoinColumn(name = "standard_reference_data1", insertable = false, updatable = false)
    val data1: ReferenceData,

    @ManyToOne
    @JoinColumn(name = "standard_reference_data2", insertable = false, updatable = false)
    val data2: ReferenceData,
)

interface LinkedListRepository : JpaRepository<LinkedList, LinkedListId> {
    fun findByData1IdIn(data1Ids: List<Long>): List<LinkedList>
}

fun CodeAndDescription.codedDescription() = CodedDescription(code, description)
fun List<CodeAndDescription>.codedDescriptions() =
    map(CodeAndDescription::codedDescription).sortedBy { it.description.lowercase() }