package uk.gov.justice.digital.hmpps.data.repository

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "iaps_offender")
class IapsPerson(
  @Id
  @Column(name = "offender_id")
  val personId: Long,

  @Column(name = "iaps_flag")
  val iapsFlag: Long
)
