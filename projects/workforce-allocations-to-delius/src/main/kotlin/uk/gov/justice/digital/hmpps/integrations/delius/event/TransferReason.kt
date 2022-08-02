package uk.gov.justice.digital.hmpps.integrations.delius.event

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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
