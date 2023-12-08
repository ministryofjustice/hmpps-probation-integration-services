package uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        SELECT rd FROM ReferenceData rd
        WHERE rd.dataset.code = :datasetCode
        AND rd.code = :code
    """,
    )
    fun findByDatasetAndCode(
        datasetCode: DatasetCode,
        code: String,
    ): ReferenceData?
}

@Immutable
@Entity
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
)
