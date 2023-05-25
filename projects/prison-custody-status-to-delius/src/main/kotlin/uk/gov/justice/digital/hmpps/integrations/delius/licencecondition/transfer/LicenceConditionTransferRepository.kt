package uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.transfer

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.transfer.entity.LicenceConditionTransfer

interface LicenceConditionTransferRepository : JpaRepository<LicenceConditionTransfer, Long> {
    fun findAllByLicenceConditionIdAndStatusCode(licenceConditionId: Long, statusCode: String): List<LicenceConditionTransfer>
}
