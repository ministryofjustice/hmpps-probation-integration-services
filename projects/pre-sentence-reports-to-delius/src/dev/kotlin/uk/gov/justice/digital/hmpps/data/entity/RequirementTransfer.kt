package uk.gov.justice.digital.hmpps.data.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "rqmnt_transfer")
class RequirementTransfer(
    @Id
    @Column(name = "rqmnt_transfer_id")
    val id: Long,

    @Column(name = "rqmnt_id")
    val requirementId: Long,

    @Column(name = "transfer_status_id")
    val statusId: Long,

    @Column(columnDefinition = "NUMBER", nullable = false)
    val softDeleted: Boolean,
)
