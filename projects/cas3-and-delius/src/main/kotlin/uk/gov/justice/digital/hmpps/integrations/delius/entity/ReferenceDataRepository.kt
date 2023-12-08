package uk.gov.justice.digital.hmpps.integrations.delius.entity

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {
    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.code = :code
    """,
    )
    fun findByCodeAndDatasetCode(
        code: String,
        datasetCode: DatasetCode,
    ): ReferenceData?
}

fun ReferenceDataRepository.findAddressStatusByCode(code: String) = findByCodeAndDatasetCode(code, DatasetCode.ADDRESS_STATUS)

fun ReferenceDataRepository.getAddressStatus(code: String): ReferenceData = findAddressStatusByCode(code) ?: throw NotFoundException("Address Status", "code", code)

fun ReferenceDataRepository.mainAddressStatus() = getAddressStatus("M")

fun ReferenceDataRepository.previousAddressStatus() = getAddressStatus("P")

fun ReferenceDataRepository.cas3AddressType() =
    findByCodeAndDatasetCode(AddressTypeCode.CAS3.code, DatasetCode.ADDRESS_TYPE)
        ?: throw NotFoundException("Address Type", "code", AddressTypeCode.CAS3.code)
