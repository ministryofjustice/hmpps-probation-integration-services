package uk.gov.justice.digital.hmpps.data.generator

import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceCondition
import uk.gov.justice.digital.hmpps.integrations.delius.licencecondition.entity.LicenceConditionTransfer
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.TransferStatusCode
import java.time.ZonedDateTime

object LicenceConditionTransferGenerator {
    val DEFAULT = generate(LicenceConditionGenerator.DEFAULT)

    fun generate(licenceCondition: LicenceCondition) = LicenceConditionTransfer(
        id = IdGenerator.getAndIncrement(),
        licenceCondition = licenceCondition,
        status = ReferenceDataGenerator.TRANSFER_STATUS[TransferStatusCode.PENDING]!!,
        requestDate = ZonedDateTime.now(),
        originTeam = TeamGenerator.DEFAULT,
        originStaff = StaffGenerator.UNALLOCATED,
        receivingTeam = TeamGenerator.DEFAULT,
        receivingStaff = StaffGenerator.UNALLOCATED
    )
}
