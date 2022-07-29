package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        SELECT rd FROM ReferenceData rd
        WHERE rd.referenceDataMaster.code = :masterCode
        AND rd.code = :code
    """
    )
    fun findByDatasetAndCode(masterCode: String, code: String): ReferenceData?
}

fun ReferenceDataRepository.findPendingTransfer() = findByDatasetAndCode("TRANSFER STATUS", "PN")

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
    val referenceDataMaster: ReferenceDataMaster
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class ReferenceDataMaster(
    @Id
    @Column(name = "reference_data_master_id")
    val id: Long,

    @Column(name = "code_set_name", length = 100, nullable = false)
    val code: String,
)
