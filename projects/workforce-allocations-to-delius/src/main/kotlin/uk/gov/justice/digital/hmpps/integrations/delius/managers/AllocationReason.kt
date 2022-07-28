package uk.gov.justice.digital.hmpps.integrations.delius.managers

import org.hibernate.annotations.Immutable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Immutable
@Entity
@Table(name = "R_STANDARD_REFERENCE_LIST")
class AllocationReason(
  @Id
  @Column(name = "STANDARD_REFERENCE_LIST_ID", nullable = false)
  var id: Long,

  @Column(name = "CODE_VALUE", length = 100, nullable = false)
  var code: String,

  @ManyToOne
  @JoinColumn(name = "REFERENCE_DATA_MASTER_ID")
  var referenceDataMaster: ReferenceDataMaster
)

@Immutable
@Entity
@Table(name = "r_reference_data_master")
class ReferenceDataMaster(
    @Id
    @Column(name = "reference_data_master_id")
    var id: Long,

    @Column(name = "code_set_name", length = 100, nullable = false)
    var code: String,
)
