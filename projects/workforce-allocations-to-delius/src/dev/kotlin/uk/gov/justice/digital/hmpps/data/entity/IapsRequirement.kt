package uk.gov.justice.digital.hmpps.data.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable

@Immutable
@Entity
@Table(name = "iaps_rqmnt")
class IapsRequirement(
    @Id
    @Column(name = "rqmnt_id")
    val requirementId: Long,

    val iapsFlag: Long
)
