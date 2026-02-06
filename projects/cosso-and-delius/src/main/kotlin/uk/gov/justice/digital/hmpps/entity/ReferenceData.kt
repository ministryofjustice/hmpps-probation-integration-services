package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.model.CodedDescription
import uk.gov.justice.digital.hmpps.model.SentenceType

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

    @ManyToMany
    @JoinTable(
        name = "r_linked_list",
        joinColumns = [JoinColumn(name = "standard_reference_data1")],
        inverseJoinColumns = [JoinColumn(name = "standard_reference_data2")]
    )
    val linkedData: Set<ReferenceData>,

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
        const val TITLE = "TITLE"
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

fun CodeAndDescription.codedDescription() = CodedDescription(code, description)
fun List<CodeAndDescription>.codedDescriptions() =
    map(CodeAndDescription::codedDescription).sortedBy { it.description.lowercase() }

fun ReferenceData.sentenceType() =
    SentenceType(
        code,
        description,
        linkedData.firstOrNull()?.description
            ?: throw IllegalStateException("ReferenceData with code '$code' has no linked data for sentence type")
    )
fun List<ReferenceData>.sentenceTypes() = map(ReferenceData::sentenceType).sortedBy { it.description }