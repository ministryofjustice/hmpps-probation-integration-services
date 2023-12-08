package uk.gov.justice.digital.hmpps.integrations.delius.event

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "r_transfer_reason")
class TransferReason(
    @Id
    @Column(name = "transfer_reason_id")
    val id: Long,
    val code: String,
)

enum class TransferReasonCode(val value: String) {
    CASE_ORDER("CASE ORDER"),
    COMPONENT("COMPONENT"),
}
