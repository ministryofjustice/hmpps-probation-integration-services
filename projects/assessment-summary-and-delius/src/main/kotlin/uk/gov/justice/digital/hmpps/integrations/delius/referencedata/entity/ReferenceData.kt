package uk.gov.justice.digital.hmpps.integrations.delius.referencedata.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(

    @Column(name = "code_value")
    val code: String,

    @Column(name = "code_description")
    val description: String,

    @Column(name = "reference_data_master_id")
    val datasetId: Long,

    @Id
    @Column(name = "standard_reference_list_id")
    val id: Long
) {
    enum class Code(val value: String) {
        OASYS_RISK_FLAG("1"),
        REGISTRATION_ADDED("probation-case.registration.added"),
        REGISTRATION_DEREGISTERED("probation-case.registration.deregistered")
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
        DOMAIN_EVENT_TYPE("DOMAIN EVENT TYPE"),
        REGISTER_TYPE_FLAG("REGISTER TYPE FLAG"),
        REGISTER_LEVEL("REGISTER LEVEL"),
    }
}

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.name = :datasetCode and rd.code = :code
        """
    )
    fun findByCode(code: String, datasetCode: String): ReferenceData?
}

fun ReferenceDataRepository.getByCode(code: String, datasetCode: String) =
    findByCode(code, datasetCode) ?: throw NotFoundException(datasetCode, "code", code)

fun ReferenceDataRepository.domainEventType(code: String) = getByCode(code, Dataset.Code.DOMAIN_EVENT_TYPE.value)

fun ReferenceDataRepository.registerLevel(code: String) = getByCode(code, Dataset.Code.REGISTER_LEVEL.value)