package uk.gov.justice.digital.hmpps.integrations.delius.approvedpremises.referral.entity

import jakarta.persistence.*
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "approved_premises_preferred")
@EntityListeners(AuditingEntityListener::class)
@SequenceGenerator(name = "ap_preferred_id_seq", sequenceName = "ap_preferred_id_seq", allocationSize = 1)
class PreferredResidence(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ap_preferred_id_seq")
    @Column(name = "approved_premises_preferred_id")
    val id: Long = 0,

    val approvedPremisesReferralId: Long
)

interface PreferredResidenceRepository : JpaRepository<PreferredResidence, Long> {
    fun existsByApprovedPremisesReferralId(approvedPremisesReferralId: Long): Boolean

    fun deleteByApprovedPremisesReferralId(approvedPremisesReferralId: Long)
}