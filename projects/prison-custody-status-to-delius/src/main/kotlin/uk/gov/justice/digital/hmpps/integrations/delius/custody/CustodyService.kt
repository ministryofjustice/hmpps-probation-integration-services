package uk.gov.justice.digital.hmpps.integrations.delius.custody

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.Custody
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistory
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyHistoryRepository
import uk.gov.justice.digital.hmpps.integrations.delius.custody.entity.CustodyRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.ReferenceDataRepository
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodialStatus
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.getCustodyEventType
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodialStatusCode
import uk.gov.justice.digital.hmpps.integrations.delius.referencedata.wellknown.CustodyEventTypeCode
import java.time.ZonedDateTime

@Service
class CustodyService(
    private val referenceDataRepository: ReferenceDataRepository,
    private val custodyRepository: CustodyRepository,
    private val custodyHistoryRepository: CustodyHistoryRepository
) {
    fun updateStatus(custody: Custody, status: CustodialStatusCode, date: ZonedDateTime, detail: String) {
        if (custody.status.code != status.code) {
            custody.status = referenceDataRepository.getCustodialStatus(status.code)
            custody.statusChangeDate = date
            custodyRepository.save(custody)
            custodyHistoryRepository.save(
                CustodyHistory(
                    date = date,
                    type = referenceDataRepository.getCustodyEventType(CustodyEventTypeCode.STATUS_CHANGE.code),
                    detail = detail,
                    person = custody.disposal.event.person,
                    custody = custody
                )
            )
        }
    }
}
