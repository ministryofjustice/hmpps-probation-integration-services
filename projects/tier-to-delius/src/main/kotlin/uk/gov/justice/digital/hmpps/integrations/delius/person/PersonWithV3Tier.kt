package uk.gov.justice.digital.hmpps.integrations.delius.person

import jakarta.persistence.*

@Entity
@Table(name = "offender")
class PersonWithV3Tier(
    @Id
    @Column(name = "offender_id")
    val id: Long,

    @Column(columnDefinition = "char(7)")
    val crn: String,

    @Column(name = "v3_tier_id")
    var v3TierId: Long? = null,

    @Version
    @Column(name = "row_version", nullable = false)
    val version: Long = 0,
)
