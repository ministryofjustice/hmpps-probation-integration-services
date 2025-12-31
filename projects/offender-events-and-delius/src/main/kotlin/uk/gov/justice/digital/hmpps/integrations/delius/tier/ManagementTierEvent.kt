package uk.gov.justice.digital.hmpps.integrations.delius.tier

import jakarta.persistence.*
import org.hibernate.annotations.Immutable
import org.springframework.data.jpa.repository.JpaRepository

@Immutable
@Entity
class ManagementTierEvent(
    @Id
    @Column(name = "management_tier_event_id", nullable = false)
    val id: Long,

    @ManyToOne
    @JoinColumn(name = "tier_change_reason_id")
    val reason: ReferenceData
)

@Immutable
@Entity
@Table(name = "r_standard_reference_list")
class ReferenceData(
    @Column(name = "code_value", length = 100, nullable = false)
    val code: String,

    @Id
    @Column(name = "standard_reference_list_id", nullable = false)
    val id: Long
)

interface ManagementTierEventRepository : JpaRepository<ManagementTierEvent, Long>
