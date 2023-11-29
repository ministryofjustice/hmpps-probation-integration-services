package uk.gov.justice.digital.hmpps.integrations.delius.referencedata

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.exception.NotFoundException
import uk.gov.justice.digital.hmpps.integrations.delius.address.AddressTypeCode.APPROVED_PREMISES

interface ReferenceDataRepository : JpaRepository<ReferenceData, Long> {

    @Query(
        """
        select rd from ReferenceData rd
        join Dataset ds on rd.datasetId = ds.id
        where ds.code = :datasetCode and rd.code = :code
    """
    )
    fun findByCodeAndDatasetCode(code: String, datasetCode: DatasetCode): ReferenceData?
}

fun ReferenceDataRepository.findAddressStatusByCode(code: String) =
    findByCodeAndDatasetCode(code, DatasetCode.ADDRESS_STATUS)

fun ReferenceDataRepository.getAddressStatus(code: String): ReferenceData =
    findAddressStatusByCode(code) ?: throw NotFoundException("Address Status", "code", code)

fun ReferenceDataRepository.mainAddressStatus() = getAddressStatus("M")
fun ReferenceDataRepository.previousAddressStatus() = getAddressStatus("P")

fun ReferenceDataRepository.approvedPremisesAddressType() =
    findByCodeAndDatasetCode(APPROVED_PREMISES.code, DatasetCode.ADDRESS_TYPE)
        ?: throw NotFoundException("Address Type", "code", APPROVED_PREMISES.code)

fun ReferenceDataRepository.referralCategory(code: String) =
    findByCodeAndDatasetCode(code, DatasetCode.AP_REFERRAL_CATEGORY)
        ?: throw NotFoundException("ReferralCategory", "code", code)

fun ReferenceDataRepository.acceptedDeferredAdmission() = findByCodeAndDatasetCode("AD", DatasetCode.REFERRAL_DECISION)
    ?: throw NotFoundException("ReferralDecision", "code", "AD")

fun ReferenceDataRepository.apReferralSource() = findByCodeAndDatasetCode("AP", DatasetCode.SOURCE_TYPE)
    ?: throw NotFoundException("SourceType", "code", "AP")

fun ReferenceDataRepository.ynUnknown() = findByCodeAndDatasetCode("D", DatasetCode.YES_NO)
    ?: throw NotFoundException("YesNo", "code", "D")

fun ReferenceDataRepository.unknownRisk() = findByCodeAndDatasetCode("K", DatasetCode.RISK_OF_HARM)
    ?: throw NotFoundException("RiskOfHarm", "code", "K")

fun ReferenceDataRepository.referralCompleted() = findByCodeAndDatasetCode("APRC", DatasetCode.NSI_OUTCOME)
    ?: throw NotFoundException("NsiOutcome", "code", "APRC")
