package uk.gov.justice.digital.hmpps.entity.sentence.component

import uk.gov.justice.digital.hmpps.entity.ReferenceData
import uk.gov.justice.digital.hmpps.entity.sentence.Disposal
import uk.gov.justice.digital.hmpps.entity.sentence.component.manager.Manager
import uk.gov.justice.digital.hmpps.entity.sentence.component.transfer.Transfer
import java.time.ZonedDateTime

interface SentenceComponent {
    val id: Long
    val disposal: Disposal
    val startDate: ZonedDateTime
    var commencementDate: ZonedDateTime?
    var terminationDate: ZonedDateTime?
    var terminationReason: ReferenceData?
    var pendingTransfer: Boolean
    val pendingTransfers: List<Transfer>
    val manager: Manager?
    var notes: String?
    val type: String
    val completedReason: ReferenceData.KnownValue
    val transferRejectionReason: ReferenceData.KnownValue
}
