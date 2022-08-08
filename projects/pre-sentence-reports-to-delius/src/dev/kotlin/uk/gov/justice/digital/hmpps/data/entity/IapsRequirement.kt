package uk.gov.justice.digital.hmpps.data.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "iaps_rqmnt")
class IapsRequirement(
    @Id
    @Column(name = "rqmnt_id")
    val requirementId: Long,

    val iapsFlag: Long
)
