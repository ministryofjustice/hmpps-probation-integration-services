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
    val commencementDate: ZonedDateTime?
    var terminationDate: ZonedDateTime?
    var terminationReason: ReferenceData?
    var pendingTransfer: Boolean
    val manager: Manager?
    val pendingTransfers: List<Transfer>
    var notes: String?
    val type: String
        get() = when (this) {
            is Requirement -> "Requirement"
            is LicenceCondition -> "Licence Condition"
            else -> "Component"
        }
    val completedReason: ReferenceData.KnownValue
    val transferRejectionReason: ReferenceData.KnownValue
}
