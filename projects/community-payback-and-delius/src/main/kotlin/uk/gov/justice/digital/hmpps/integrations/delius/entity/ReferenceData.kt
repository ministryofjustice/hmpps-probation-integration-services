package uk.gov.justice.digital.hmpps.integrations.delius.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.model.CodeDescription

@Entity
@Immutable
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Column(name = "reference_data_master_id", nullable = false)
    val datasetId: Long,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean
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
) {
    companion object {
        const val UPW_PROJECT_TYPE = "UPW PROJECT TYPE"
        const val UPW_WORK_QUALITY = "UPW WORK QUALITY"
        const val UPW_BEHAVIOUR = "UPW BEHAVIOUR"
    }
}

@Immutable
@Entity
@Table(name = "r_contact_outcome_type")
class ContactOutcome(
    @Id
    @Column(name = "contact_outcome_type_id")
    val id: Long,

    @Column(name = "code")
    val code: String,

    @Column(name = "description")
    val description: String
)

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.code = :code
    """
    )
    fun findByCodeAndDatasetCode(code: String, datasetCode: String): ReferenceData?

    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.selectable = true
    """
    )
    fun findByDatasetCode(datasetCode: String): List<ReferenceData>
}

fun ReferenceData.toCodeDescription() = CodeDescription(
    code = this.code,
    description = this.description
)