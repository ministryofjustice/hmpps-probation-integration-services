package uk.gov.justice.digital.hmpps.entity

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.hibernate.type.YesNoConverter
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Convert(converter = YesNoConverter::class)
    val selectable: Boolean,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long,

    @Column(name = "reference_data_master_id")
    val dataSetId: Long,
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class ReferenceDataSet(

    @Column(name = "code_set_name")
    val name: String,

    @Id
    @Column(name = "reference_data_master_id")
    val id: Long
) {
    enum class Code(val value: String) {
        BREACH_REASON("BREACH REASON")
    }
}

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd
        join ReferenceDataSet ds on rd.dataSetId = ds.id
        where ds.name = :dataSetName
        order by rd.code
    """
    )
    fun findAllByDataSetName(dataSetName: String): List<ReferenceData>
}


