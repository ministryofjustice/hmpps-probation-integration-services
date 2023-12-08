package uk.gov.justice.digital.hmpps.integrations.delius.allocations.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "offender_transfer")
class PersonTransfer(
    @Id
    @Column(name = "offender_transfer_id")
    val id: Long,
    @Column(name = "offender_id")
    val personId: Long,
    @Column(name = "transfer_status_id")
    val statusId: Long,
    @Column(columnDefinition = "NUMBER", nullable = false)
    val softDeleted: Boolean,
)
