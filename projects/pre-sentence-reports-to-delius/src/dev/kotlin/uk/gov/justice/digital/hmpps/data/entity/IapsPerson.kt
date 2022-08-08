package uk.gov.justice.digital.hmpps.data.entity

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "iaps_offender")
class IapsPerson(
    @Id
    @Column(name = "offender_id")
    val personId: Long,

    val iapsFlag: Long
)
