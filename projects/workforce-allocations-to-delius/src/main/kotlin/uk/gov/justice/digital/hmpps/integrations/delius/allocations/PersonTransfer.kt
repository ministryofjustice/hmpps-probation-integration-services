package uk.gov.justice.digital.hmpps.integrations.delius.allocations

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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
